
package sx.inventario


import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Inventario
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class MovimientoDeAlmacenController extends RestfulController {

    static responseFormats = ['json']

    ReportService reportService

    MovimientoDeAlmacenController() {
        super(MovimientoDeAlmacen)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = MovimientoDeAlmacen.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }
        if(params.documento) {
            def documento = params.int('documento')

            query = query.where {documento ==  documento}
        }
        
        return query.list(params)
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
                renglon++
            }
            resource.fechaInventario = new Date()

        }

        return super.updateResource(resource)
    }

    def print() {
        // log.debug('Imprimiendo movimiento: {}', params.ID)
        def pdf =  reportService.run('MovGenerico.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }
}
