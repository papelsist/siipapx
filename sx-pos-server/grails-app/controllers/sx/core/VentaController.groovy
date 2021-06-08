package sx.core

import com.luxsoft.utils.ImporteALetra
import com.luxsoft.utils.Periodo
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.transform.ToString
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.http.HttpStatus
import sx.inventario.SolicitudDeTrasladoService
import sx.logistica.CondicionDeEnvio
import sx.logistica.Envio
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class VentaController extends RestfulController{

    static responseFormats = ['json']

    VentaService ventaService

    ReportService reportService

    SolicitudDeTrasladoService solicitudDeTrasladoService

    VentaController(){
        super(Venta)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 50
        def query = Venta.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if (params.facturables) {
            query = query.where {facturar !=  null  && cuentaPorCobrar == null}
            if(params.facturables == 'CRE'){
                query = query.where {tipo == params.facturables}
            } else {
                query = query.where {tipo != 'CRE'}
            }
        }
        /*
        if (params.facturados) {
            log.debug('Buscando pedidos facturados {}', params)
            query = query.where {cuentaPorCobrar != null}
        }
        */
        def list = query.list(params)
        return list
    }


    @Transactional
    def mandarFacturar() {
        log.debug('Mandando facturar: ' + params)
        def res = ventaService.mandarFacturar(params.id, params.usuario)
        respond res
    }

    @Transactional
    def regresaraPendiente(Venta venta) {
        if(venta == null) {
            notFound()
            return
        }
        def res = ventaService.regresaraPendiente(venta)
        respond res
        
    }

    @Transactional
    def mandarFacturarConAutorizacion(AutorizacionDeVenta autorizacion) {
        // log.debug('Mandando facturar con autorizacion: {}', autorizacion)
        def res = ventaService.mandarFacturar(autorizacion.venta.id, autorizacion.autorizo)
        autorizacion.save failOnError: true, flush: true
        respond res
    }

    @Transactional
    def asignarEnvio() {
        Venta venta = Venta.get(params.id)
        log.debug('Asignando envio para venta: {}', venta)
        Direccion direccion = new Direccion()
        bindData direccion, getObjectToBind()
        log.debug('Direccion asignada: {}', direccion)
        if(venta.envio) {
            venta.envio.direccion = direccion
        } else {
            log.debug('Venta sin envio, generando un envio nuevo para direccion {}', direccion)
            CondicionDeEnvio envio = new CondicionDeEnvio()
            envio.direccion = direccion
            envio.condiciones = 'ENVIO'
            envio.venta = venta
            venta.envio = envio
            venta = venta.save()
        }
        respond venta
    }

    @Transactional
    def cancelarEnvio() {
        Venta venta = Venta.get(params.id)
        log.debug('Cancelar envio para venta: {}', venta)

        def asignada=Envio.findAllByOrigen(venta.id)

        if(venta.envio ) {

            if(!asignada){
                    CondicionDeEnvio condicionDeEnvio= venta.envio
                    venta.envio = null
                    venta.cod= false
                    venta = venta.save failOnError: true, flush:true
                    condicionDeEnvio.venta = null
                    
                    condicionDeEnvio.delete failOnError: true, flush: true
            }else{
                println "Venta ya asignada no se puede cancelar el envio"
            }
            
        }
        respond venta
    }


    @Override
    protected Object createResource() {
        Venta venta = new Venta()
        bindData venta, getObjectToBind()
        venta.partidas.each {
            if(it.corte)
                it.corte.ventaDet = it;
        }
        if(venta.envio) {
            venta.envio.venta = venta;
        }
        return venta
    }


    protected Venta saveResource(Venta resource) {
        log.debug('Salvando venta {}', resource.statusInfo())
        return ventaService.save(resource)
    }

    protected Venta updateResource(Venta resource) {
        log.debug('Actualizando venta: {}', resource)
        return ventaService.save(resource)
    }



    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        
        log.info('Pendientes: {}', params)
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar == null && facturar == null }

        Boolean callcenter = params.getBoolean('callcenter', false)
        if(callcenter) {
            log.info('Ventas de callcenter: {}', callcenter)
            query = query.where{callcenter == true}
        }
        else 
            query = query.where{callcenter == false}
        if( params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            query = query.where{ fecha >= periodo.fechaInicial && fecha<= periodo.fechaFinal}
        }
        try{
            if(params.term) {
                def search = '%' + params.term + '%'
                //
                if(params.term.isNumber() ) {
                    if(params.term.isInteger() ) {
                        query = query.where{documento == params.term.toInteger()}
                    }else{
                       query = query.where{total == new BigDecimal(params.term)} 
                    }
                } else {
                    query = query.where { nombre =~ search || createUser =~ search }
                }
            }
        }catch(Exception e){
            e.printStackTrace()
        }
      
        respond query.list(params)
    }


    def facturados(Sucursal sucursal) {
        log.debug('Buscando facturas {}', params )
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:50
        params.sort = params.sort ?:'documento'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null }
        if(params.boolean('canceladas')) {
            log.debug('Facturas canceladas {}', params)
            query = query.where {cuentaPorCobrar.cancelada != null}
        }

        if(params.cliente) {
            def search = '%' + params.cliente + '%'
            query = query.where { nombre =~ search }
        }
        if(params.usuario) {
            def search = '%' + params.usuario + '%'
            query = query.where { updateUser =~ search }
        }
        if( params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            query = query.where{ cuentaPorCobrar.fecha >= periodo.fechaInicial && cuentaPorCobrar.fecha<= periodo.fechaFinal}
        }

        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{cuentaPorCobrar.documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    def findManiobra() {
        def clave = params.clave
        def found = Producto.where{ clave == clave}.find()
        if(found == null ){
            notFound()
            return
        }
        respond found
    }

    @Transactional
    def facturar() {
        Venta pedido = Venta.get(params.id)
        assert !pedido.cuentaPorCobrar, 'Pedido ya facturado'
        pedido = ventaService.facturar(pedido);
        respond pedido
    }

    @Transactional
    def registrarPuesto() {
        Venta venta = Venta.get(params.id)
        bindData venta, getObjectToBind()
        venta = ventaService.registrarPuesto(venta, params.usuario)
        respond venta
    }
    
    @Transactional
    def quitarPuesto() {
        Venta venta = Venta.get(params.id)
        bindData venta, getObjectToBind()
        venta = ventaService.quitarPuesto(venta, params.usuario)
        respond venta   
    }
 
    /**
     * Facturas de Contado, es decir tipo CON and COD
     * @param sucursal
     * @return
     */
    def cobradas(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        // log.debug('Buscando facturas contado : {}', params)
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null}
        query = query.where { tipo != 'CRE'}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{cuentaPorCobrar.documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    // @Transactional
    def timbrar() {
        Venta venta = Venta.get(params.id)
        try {
            
             def cfdi = venta.cuentaPorCobrar.cfdi

             if(!cfdi){
                 cfdi = ventaService.generarCfdi(venta)
             }
             
                cfdi = ventaService.timbrar(venta)
            respond cfdi
            return
        }catch (Exception ex) {
            ex.printStackTrace()
            String m = ExceptionUtils.getRootCauseMessage(ex)
            respond( [message: m], status: HttpStatus.NOT_ACCEPTABLE )
        }
    }

    def print( ) {
        Venta pedido = Venta.get(params.id)
        params.ID = pedido.id
        params.IMP_CON_LETRA = ImporteALetra.aLetra(pedido.total)
        params.TELEFONOS = pedido.cliente.getTelefonos().join('/')
        if(pedido.envio) {
            params.DIR_ENTREGA = pedido.envio.direccion.toLabel()
        }
        if(pedido.socio) {
            params.SOCIO = pedido.socio.nombre
        }

        def pdf =  reportService.run('Pedido.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }

    /**
     * Cancela la venta, que implica eliminar la cuenta por cobrar pero deja el pedido  vivo
     *
     * @param venta
     * @return
     */
    def cancelar(Venta venta) {
        if(venta == null ){
            notFound()
            return
        }
        if (!venta.cuentaPorCobrar) {
            respond( [message: 'NO ESTA FACTURADA'], status: HttpStatus.NOT_ACCEPTABLE )
            return
        }
        String usuario = params.usuario
        String motivo = params.motivo
        // log.debug('Canccelando con: {}', params)

        try {
            def res = ventaService.cancelarFactura(venta, usuario, motivo)
            respond res
            return
        }catch (Exception ex) {
            respond( [message: ExceptionUtils.getRootCauseMessage(ex)], status: HttpStatus.NOT_ACCEPTABLE )
        }

        // respond venta

    }

    def cambioDeCliente(Venta venta){
        if(venta == null ){
            notFound()
            return
        }
        assert venta.cuentaPorCobrar == null , 'La venta esta facturada no se permite cambio de cliente'
        assert venta.tipo != 'CRE', 'Venta de credito no permite modificar cliente'
        assert venta.descuento <= venta.descuentoOriginal  ,
                "Venta con descuento especial, no permite cambio de cliente " +
                        " Dsc Original ${venta.descuentoOriginal} Desc: ${venta.descuento}"
        Cliente cliente = Cliente.get(params.cliente)
        String usuario = params.usuario
        venta.cliente = cliente
        venta.nombre = cliente.nombre
        venta.updateUser = usuario
        venta.save flush: true
        respond venta
    }

    def pedidosPendientes(Cliente cliente){
        if(cliente== null ){
            notFound()
            return
        }
        Sucursal sucursal = AppConfig.first().sucursal
        Date hoy = new Date()
        Date fechaInicial = hoy - 14
        def query = Venta.where{
            cliente == cliente && sucursal == sucursal && cuentaPorCobrar == null && facturar == null && fecha >= fechaInicial && fecha <= hoy
        }
        respond query.list(params)
    }

    def buscarPartidas(Venta venta){

        def partidas=[];

        println "Buscando facturas"

        venta.partidas.each{det ->

            def part = new Partidas()

            part.noIdentificacion=det.producto.clave
            part.descripcion= det.producto.descripcion
            part.unidad= det.producto.unidad
            part.cantidad=det.cantidad
            part.comentario=det.comentario
            part.instruccion=det.corte? det.corte.instruccion : ''

            partidas.add(part)

        }

        respond partidas

    }

    def noFacturables(){
        def pedidos=Venta.where{sucursal == sucursal && cuentaPorCobrar == null && facturar == null && fecha > (new Date()-45) && noFacturable==false}

        pedidos.each{ pedido ->
          //  println pedido.documento+"  "+ pedido.fecha
            pedido.noFacturable=true;
            pedido.save failOnError:true, flush:true
        }
    }

    def generarValeAutomatico(){
        println "Generando vale automatico para: "+params.id
        solicitudDeTrasladoService.generarValeAutomatico(params.id)
        return {}
    }

    /**
     * Deletes a resource for the given id
     * @param id The id
     */
    @Transactional
    def delete() {
        if(handleReadOnly()) {
            return
        }
        def venta = Venta.get(params.id)
        if (venta == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }
        this.ventaService.delete(venta)
        render status: HttpStatus.NO_CONTENT
    }

    @Transactional
    def regresarCallcenter() {
        if(handleReadOnly()) {
            return
        }
        def venta = Venta.get(params.id)
        if (venta == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }
        this.ventaService.regresarCallcenter(venta, params.usuario)
        render status: HttpStatus.NO_CONTENT
    }

}

@ToString(includeNames=true,includePackage=false)
class VentasFiltro {

    Date fechaInicial
    Date fechaFinal
    Sucursal sucursal
    Cliente cliente
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        sucursal nullable:true
        cliente nullable:true
        registros size:(1..500)
    }
}


@ToString(includeNames=true,includePackage=false)
class Partidas{
    String noIdentificacion
    String descripcion
    String unidad
    BigDecimal cantidad
    String comentario
    String instruccion

}
