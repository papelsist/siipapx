
package sx.inventario


import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.ExistenciaService
import sx.core.Folio
import sx.core.Inventario
import sx.reports.ReportService
import sx.core.VentaDet
import com.luxsoft.utils.Periodo

@Secured("ROLE_INVENTARIO_USER")
class MovimientoDeAlmacenController extends RestfulController {

    static responseFormats = ['json']

    ReportService reportService

    ExistenciaService existenciaService

    MovimientoDeAlmacenController() {
        super(MovimientoDeAlmacen)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = MovimientoDeAlmacen.where {sucursal == AppConfig.first().sucursal}

        if(params.boolean('pendientes')) {
            query = query {fechaInventario == null}
        }

        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { documento == params.term.toInteger() }
            } else {
                query = query.where { comentario =~ search}
            }
        }
        return query.list(params)
    }


     def List puestos(params){
        println('Buscando puestos ' + params)
        
        params.sort = 'lastUpdated'
        params.order = 'desc'
         params.max = 500

        
        def query = VentaDet.where { venta.puesto != null && venta.cuentaPorCobrar == null && producto.inventariable == true}
        if(params.term) {
            //def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { venta.documento == params.term.toInteger() }
            } else {
                query = query.where { producto.clave =~  params.term}
            }
        }
        if( params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            query = query.where{ venta.fecha >= periodo.fechaInicial && venta.fecha<= periodo.fechaFinal}
        }
        respond query.list(params)
        
    }

    // @Override
    protected MovimientoDeAlmacen saveResource(MovimientoDeAlmacen resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('MOVIMIENTO',serie)
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected MovimientoDeAlmacen updateResource(MovimientoDeAlmacen resource) {

        if(params.inventariar){
            def renglon = 1;
            resource.partidas.each { det ->
                Inventario inventario = new Inventario()
                inventario.sucursal = resource.sucursal
                inventario.documento = resource.documento
                inventario.cantidad = det.cantidad
                inventario.comentario = det.comentario
                inventario.fecha = resource.fecha
                inventario.producto = det.producto
                inventario.tipo = resource.tipo
                inventario.renglon = renglon
                det.inventario = inventario
                existenciaService.afectarExistenciaEnAlta(inventario)
                renglon++
            }
            resource.fechaInventario = new Date()

        }

        return super.updateResource(resource)
    }

    def print() {
        log.debug('Imprimiendo recepcion de compra: {}', params.ID)
        def pdf =  reportService.run('MovGenerico.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }

 
}
