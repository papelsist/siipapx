package sx.compras


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario
import sx.core.Sucursal

@Secured("ROLE_INVENTARIO_USER")
class DevolucionDeCompraController extends RestfulController {

    static responseFormats = ['json']

    DevolucionDeCompraController() {
        super(DevolucionDeCompra)
    }

    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = DevolucionDeCompra.where {}
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {documento ==  documento}
        }
        if(params.sucursal){
        	query = query.where {sucursal.id ==  params.sucursal}	
        }
        return query.list(params)
    }

    // @Override
    protected DevolucionDeCompra saveResource(DevolucionDeCompra resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('DECS',serie)
            resource.createUser = username
        }
        resource.partidas.each {
            it.comentario = resource.comentario
        }
        resource.referencia = resource.recepcionDeCompra.remision
        resource.fechaReferencia = resource.recepcionDeCompra.fechaRemision
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected DevolucionDeCompra updateResource(DevolucionDeCompra resource) {
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
                inventario.tipo = 'DEC'
                inventario.renglon = renglon++
                det.inventario = inventario
            }
            resource.fechaInventario = new Date()
        }

        return super.updateResource(resource)
    }

    public buscarCom(ComParaDecSearchCommand command){
       
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = RecepcionDeCompra.where{sucursal == command.sucursal && documento == command.documento }
        
        RecepcionDeCompra res = q.find()

        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'recepcionDeCompra', action: 'show', id: res.id
    }


}

class ComParaDecSearchCommand {

    Sucursal sucursal
    Long documento

    String toString(){
        return "$sucursal $documento"
    }

    static constraints = {
        
    }
}

