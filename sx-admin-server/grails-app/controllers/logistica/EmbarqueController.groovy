package sx.logistica

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

import sx.core.Folio
import sx.core.Sucursal
import sx.core.Venta
import sx.inventario.Traslado
import sx.inventario.DevolucionDeVenta


@Secured("ROLE_INVENTARIO_USER")
class EmbarqueController extends RestfulController {
    
    static responseFormats = ['json']

    
    def reporteService

    EmbarqueController() {
        super(Embarque)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'documento'
        params.order = 'desc'
        params.max = 500
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
        return query.list(params)
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
        println 'Actualizando embarque ' + resource
        // Actualizar la condicion asignado
        
        resource.partidas.each { 
            if(it.entidad == 'VENTA'){ 
                def ventaId = it.origen
                def parcial = it.parcial
                println "Actualizando condicion de venta atendido ${ventaId} Parcial: ${parcial}" 
                def condicion = CondicionDeEnvio.where{venta.id == ventaId}.find()
                if(!parcial){
                    condicion.asignado = resource.fecha
                    condicion.save()
                } else {
                    condicion.parcial = true
                    condicion.save()
                }
                
                /*
                if(condicion) {
                    def venta = it.venta
                    def pendiente = venta.partidas.sum {det -> (det.cantidad.abs() - det.enviado)}
                    println 'Pendientes: '+pendientes
                    
                    if(pendiente <= 0) {
                        condicion.asignado = resource.fecha    
                    }
                    
                }
                */
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
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && 
            venta.documento == command.documento &&
            venta.fecha == command.fecha
        }
        q = q.where {
            asignado == null || (asignado != null && parcial == true)
        }
        CondicionDeEnvio res = q.find()
        if (res == null) {
            notFound()
            return
        }
        // println 'Condicion encontrada: ' + res.venta
        def venta = res.venta
        // determinando si la venta ya tiene envios
        def isParcial = venta.partidas.find { it.enviado} ? true : false
        println 'El envio debe ser parcial: ' + isParcial
        def envio = new Envio()
        envio.cliente = venta.cliente
        envio.tipoDocumento = venta.tipo
        envio.origen = venta.id
        envio.entidad = 'VENTA'
        envio.documento = venta.documento
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
        println 'Cargando envio para  traslado con: ' + command
        /*
        def q = Traslado.where{
            sucursal == command.sucursal && 
            documento == command.documento && 
            fecha == command.fecha &&
            tipo == 'TPS'
        }
        */
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

    private buscarDevolucion(DocumentSearchCommand command){

    }

    public buscarVenta(DocumentSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && venta.documento == command.documento && venta.fecha == command.fecha
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
        
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && venta.documento == command.documento && venta.fecha == command.fecha
        }
        CondicionDeEnvio res = q.find()
        if (res == null) {
            notFound()
            return
        }
        def partidas = res.venta.partidas.findAll { it.producto.inventariable == true}
        respond partidas, status: 200
    }


    @Transactional
    def registrarSalida(Embarque res) {

        if (res == null) {
            notFound()
            return
        }
        log.info('Registrando slaida del embarque: {}', res.docmento)
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
        // println 'Generando impresion para trs: '+ params
        def pdf = this.reporteService.run('AsignacionDeEnvio', params)
        def fileName = "AsignacionDeEnvio.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteDeEntregasPorChofer(EntregasPorChofferReport command) {
        
        def repParams = [:]
        repParams['CHOFER'] = command.chofer.id
        repParams['SUCURSAL'] = command.sucursal.id
        repParams['FECHA'] = command.fecha.format('yyyy/MM/dd')
        println 'Ejecutando reporte de engregas por chofer con parametros: ' + repParams
        def pdf = this.reporteService.run('EntregaPorChofer', repParams)
        def fileName = "EntregaPorChofer.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def documentosEnTransito() {
        def envios = Envio.where{ embarque.regreso ==null && embarque.salida != null}.list()
        println 'Envios pendientes: ' + envios
        respond envios
    }


    def enviosPendientes() {

        def list= [] 
        def q = CondicionDeEnvio.where{
            asignado == null || (asignado != null && parcial == true)
        }
         q.list().each{
             if(it.venta.kilos!= Envio.findAllByOrigen(it.venta.id).sum(it.kilos)) {
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
            envio.tipoDocumento = venta.tipo
            envio.origen = venta.id
            envio.entidad = 'VENTA'
            envio.documento = venta.documento
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

        // resource.partidas.each { 
        //     if(it.entidad == 'VENTA'){ 
        //         def ventaId = it.origen
        //         def parcial = it.parcial
        //         println "Actualizando condicion de venta atendido ${ventaId} Parcial: ${parcial}" 
        //         def condicion = CondicionDeEnvio.where{venta.id == ventaId}.find()
        //         if(!parcial){
        //             condicion.asignado = resource.fecha
        //             condicion.save()
        //         } else {
        //             condicion.parcial = true
        //             condicion.save()
        //         }
        //     }
            
        // }
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