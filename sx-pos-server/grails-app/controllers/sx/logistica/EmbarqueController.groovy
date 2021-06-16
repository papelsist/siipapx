package sx.logistica

import com.luxsoft.utils.MonedaUtils
import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Sucursal
import sx.core.Venta
import sx.core.VentaDet
import sx.inventario.Traslado
import sx.inventario.DevolucionDeVenta
import sx.reports.ReportService


@Secured("ROLE_INVENTARIO_USER")
class EmbarqueController extends RestfulController {
    
    static responseFormats = ['json']

    ReportService reportService

    EmbarqueController() {
        super(Embarque)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'documento'
        params.order = 'desc'
        params.max = 500
        // log.info('Localizando embarques: {}', params)
        def query = Embarque.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {documento >=  documento}
        }
        if(params.transito) {
            query = query.where{regreso == null && salida != null}
        }
        if(params.regresos) {
            query = query.where{regreso != null }
        }
        def list =  query.list(params)
        return list
    }

    protected Embarque saveResource(Embarque resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('EMBARQUES',serie)
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Embarque updateResource(Embarque resource) {
        resource.partidas.each { Envio it ->
            if(it.entidad == 'VENTA'){ 
                def ventaId = it.origen
                def parcial = it.parcial
                def condicion = CondicionDeEnvio.where{venta.id == ventaId}.find()
                if(!parcial){
                    condicion.asignado = resource.fecha
                    condicion.save()
                } else {
                    condicion.parcial = true
                    condicion.save()
                }
                if(condicion.venta.callcenter) {
                    it.callcenter = condicion.venta.sw2
                    it.callcenterVersion = condicion.venta.callcenterVersion ?: 1

                }
                // Actualizando valor
                if (!it.partidas) {
                    // log.debug('Calculando valor de evnio TOTAL')
                    Venta venta = Venta.get(it.origen)
                    it.valor = venta.subtotal
                    // FIX PENDIENTE DE VALIDAR
                    it.documento = venta.cuentaPorCobrar.documento
                    it.fechaDocumento = venta.fecha
                    it.totalDocumento = venta.total
                } else {
                    // log.debug(' Calculando valor de envio PARCIAL ')
                    def kilos = 0.0
                    it.partidas.each { EnvioDet det ->
                        def factor = det.ventaDet.producto.unidad == 'MIL' ? 1000 : 1
                        def rv  = (det.cantidad * det.ventaDet.precio)/ factor
                        def valDet = ( (100 - det.ventaDet.descuento) * rv ) /  100
                        det.valor = MonedaUtils.round(valDet, 2)
                        det.kilos = (det.cantidad * det.ventaDet.producto.kilos)/ factor
                    }
                    it.kilos = it.partidas.sum(0.0, { EnvioDet rr -> rr.kilos})
                    it.valor = it.partidas.sum(0.0, { EnvioDet rr -> rr.valor})
                }


            }
        }
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

    public buscarDocumento(DocumentSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def envio = null
        if(command.tipo == 'VENTA'){
            envio = cargarEnvioParaVenta(command)
        } else if ( command.tipo == 'TRASLADO') {
            envio = cargarEnvioParaTraslado(command)
        } else {
            envio = buscarDevolucion(command)
        }
        if(envio == null){
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        respond envio, status: 200
    }



    private cargarEnvioParaVenta(DocumentSearchCommand command){
        log.debug('Buscando venta: {}', command)

        def params=[command.sucursal,command.documento,command.fecha]
        def condicion = CondicionDeEnvio.find(
            " from CondicionDeEnvio c  " +
            " where c.venta.sucursal=? " +
            "  and c.venta.cuentaPorCobrar.documento = ? " +
            "  and c.venta.cuentaPorCobrar.fecha = ? " + 
            "  and (asignado = null or parcial = true) ",
            params)
       
        CondicionDeEnvio res = condicion
        if (res == null) {
            notFound()
            return
        }
        // println 'Condicion encontrada: ' + res.venta
        def venta = res.venta
        // determinando si la venta ya tiene envios
        def isParcial = venta.partidas.find { it.enviado} ? true : false
        def envio = new Envio()
        envio.cliente = venta.cliente
        envio.tipoDocumento = venta.cuentaPorCobrar.tipo
        envio.origen = venta.id
        envio.entidad = 'VENTA'
        envio.documento = venta.cuentaPorCobrar.documento
        envio.fechaDocumento = venta.fecha
        envio.totalDocumento = venta.total
        envio.formaPago = venta.formaDePago
        envio.nombre = venta.cliente.nombre
        envio.kilos = venta.kilos
        envio.parcial = isParcial

        return envio
    }

    private buscarTraslado(sucursal, documento, fecha) {

        def q = Traslado.where{
            sucursal == sucursal && 
            documento == documento && 
            fecha == fecha &&
            tipo == 'TPS'
        }
        return q.find()
    }

    private cargarEnvioParaTraslado(DocumentSearchCommand command) {
        def traslado = buscarTraslado(command.sucursal, command.documento. command.fecha)
        if(!traslado) {
            return null
        }
        def envio = new Envio()
        envio.tipoDocumento = 'TPS'
        envio.origen = traslado.id
        envio.entidad = 'TRASLADO'
        envio.documento = traslado.documento
        envio.fechaDocumento = traslado.fecha
        envio.totalDocumento = 0.0
        envio.formaPago = 'EFECTIVO'
        envio.nombre = traslado.solicitudDeTraslado.createUser?:'NA'
        envio.kilos = traslado.kilos
        envio.parcial = false
        return envio
    }

    private buscarDevolucion(DocumentSearchCommand command){}

    public buscarVenta(DocumentSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && venta.cuentaPorCobrar.documento == command.documento && venta.fecha == command.fecha
        }
        CondicionDeEnvio res = q.find()
        if (res == null) {
            notFound()
            return
        }
        respond res.venta, status: 200
    }

    public buscarPartidasDeVenta(DocumentSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        log.debug('Buscando partidas para la venta: {} ', command)
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && venta.cuentaPorCobrar.documento == command.documento && venta.fecha == command.fecha
        }
        CondicionDeEnvio res = q.find()
        /*
        if (res == null) {
            notFound()
            return
        }
        */
        def partidas = res.venta.partidas.findAll { it.producto.inventariable == true}
        respond partidas, status: 200
    }


    @Transactional
    def registrarSalida(Embarque res) {

        if (res == null) {
            notFound()
            return
        }
        log.info('Registrando slaida del embarque: {}', res.documento)
        res.salida = new Date()
        res.partidas.each {it.salida = res.salida}
        res.save()
        respond res
    }

    @Transactional
    def registrarRegreso(Embarque res) {
        if (res == null) {
            notFound()
            return
        }
        def found = res.partidas.find { it.recepcion == null}
        if(found ) {
            respond([message: 'Faltan envios por recibir no se puede marcar regreso'], status: 422)
            return
        }
        res.regreso = new Date()
        res.save()
        respond res
    }

    def print() {
        println 'Generando impresion para trs: '+ params
        def pdf = this.reportService.run('AsignacionDeEnvio', params)
        def fileName = "AsignacionDeEnvio.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteDeEntregasPorChofer(EntregasPorChofferReport command) {
        
        def repParams = [:]
        repParams['CHOFER'] = command.chofer.id
        repParams['SUCURSAL'] = command.sucursal.id
        repParams['FECHA'] = command.fecha.format('yyyy/MM/dd')
        println 'Ejecutando reporte de engregas por chofer con parametros: ' + repParams
        def pdf = this.reportService.run('EntregaPorChofer', repParams)
        def fileName = "EntregaPorChofer.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteFacturaEnvio(){

        def venta=Venta.get(params.id)

        def reportName="EntregaPorChofer.pdf"

        def condicion=CondicionDeEnvio.findByVenta(venta)

        if(condicion){

            if(!condicion.asignado){
                reportName="FacturaPorAsignar"
            }
            if(condicion.asignado && condicion.parcial){
                reportName="EntregaParcialFactura"
            }
            if(condicion.asignado && !condicion.parcial){
                reportName="EntregaTotalFactura"
            }

            def repParams = [:]
            repParams['ID'] = params.id
            def pdf = this.reportService.run(reportName, repParams)
            def fileName = "FacturaEnvio.pdf"
            render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

        }else{
            notFound()
            return
        }

    }

    def documentosEnTransito() {
        def envios = Envio.where{ embarque.regreso ==null && embarque.salida != null}.list()
        respond envios
    }
 

    def enviosPendientes() {
        log.info('Buscando envios pendientes')
        def list=[]
        def q = CondicionDeEnvio.where{
           asignado == null || (asignado != null && parcial == true  )
        }
        q = q.where { venta.cuentaPorCobrar != null}
        q.list().each{
            def enviado=Envio.findAllByOrigen(it.venta.id).sum{it.kilos}
            if(it.venta.kilos!= enviado ){
                list.add(it)
            }
        }
        respond list 
    }

    def buscarTrasladosPendientes() {
        def sucursal = params.sucursal
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = 300
        params.sort =  'lastUpdated'
        params.order = 'desc'
        def list = Traslado.where { tipo == 'TPS' && sucursal.id == sucursal && asignado == null}.list(params)
        respond list, status: 200
    }

    def buscarDevolucionesPendientes() {
        def sucursal = params.sucursal
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = 300
        params.sort =  'lastUpdated'
        params.order = 'desc'
        def list = DevolucionDeVenta.where { sucursal.id == sucursal && fechaInventario == null && asignado == null }.list(params)
        respond list, status: 200
    }

     @Transactional
    def asignarFacturas(AsignacionDeFacturas res) {
        if (res == null) {
            notFound()
            return
        }
        def embarque = res.embarque
        res.condiciones.each { cn ->
            CondicionDeEnvio condicion = CondicionDeEnvio.get(cn.id)
            def venta = Venta.get(cn.venta.id)
            def isParcial = cn.parcial
            def envio = new Envio()
            envio.cliente = venta.cliente
            envio.tipoDocumento = venta.cuentaPorCobrar.tipo
            envio.origen = venta.id
            envio.entidad = 'VENTA'
            envio.documento = venta.cuentaPorCobrar.documento
            envio.fechaDocumento = venta.fecha
            envio.totalDocumento = venta.total
            envio.formaPago = venta.formaDePago
            envio.nombre = venta.cliente.nombre
            envio.kilos = venta.kilos
            envio.parcial = isParcial
            embarque.addToPartidas(envio)
            condicion.asignado = new Date()
            condicion.save()
        }
        respond embarque, status:200
    }

}

class DocumentSearchCommand {
    String tipo
    Date fecha
    Sucursal sucursal
    Long documento

    String toString(){
        "Tipo:$tipo Docto:$documento Fecha:${fecha.format('dd/MM/yyyy')} Sucursal:$sucursal"
    }
}

class EntregasPorChofferReport {
    Date fecha
    Chofer chofer
    Sucursal sucursal

    String toString(){
        return "$fecha ${chofer.nombre} ${sucursal.nombre}"
    }
}

class AsignacionDeFacturas {
    Embarque embarque
    List condiciones

}