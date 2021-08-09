package sx.inventario

import groovy.util.logging.Slf4j

import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Inventario
import sx.core.Sucursal
import sx.core.Venta

@Secured("ROLE_INVENTARIO_USER")
@Slf4j
class DevolucionDeVentaController extends RestfulController<DevolucionDeVenta> {

    static responseFormats = ['json']

    DevolucionDeVentaController() {
        super(DevolucionDeVenta)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        log.info('GET {}', params)
        def query = DevolucionDeVenta.where {}
        if(params.documento) {
            def documento = params.int('documento')

            query = query.where {documento >=  documento}
        }
        if(params.sucursal) {
            query = query.where { sucursal.id == params.sucursal}
        }
        if (params.periodo) {
            def periodo = params.periodo
            query = query.where {fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }
        if (params.cartera) {
            def cartera = params.cartera
            query = query.where {venta.tipo == cartera}
        }
        return query.list(params)
    }

    // @Override
    protected DevolucionDeVenta saveResource(DevolucionDeVenta resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('RMD',serie)
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }


    public buscarVenta(VentaSearchCommand command){
       
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = Venta.where{sucursal == command.sucursal && cuentaPorCobrar.documento == command.documento && tipo == command.tipo}
        if(command.fecha){
            q = q.where { fecha == command.fecha}
        }
        Venta res = q.find()
        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'venta', action: 'show', id: res.id
    }

    protected DevolucionDeVenta updateResource(DevolucionDeVenta resource) {
        if(params.inventariar){
            resource.partidas.each { det ->
                Inventario inventario = new Inventario()
                inventario.sucursal = resource.sucursal
                inventario.documento = resource.documento
                inventario.cantidad = det.cantidad
                inventario.comentario = det.comentario
                inventario.fecha = resource.fecha
                inventario.producto = det.producto
                inventario.tipo = 'RMD'
                det.inventario = inventario
            }
            resource.fechaInventario = new Date()
        }
        return super.updateResource(resource)
    }

}

class VentaSearchCommand {

    Sucursal sucursal
    String tipo
    Long documento
    Date fecha

    String toString(){
        return "$sucursal $tipo $documento - ${fecha?.format('dd/MM/yyyy')}"
    }

    static constraints = {
        fecha nullable:true
        tipo inList: ['CRE','CON','COD']
    }
}
