package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SucursalController extends RestfulController{

    static responseFormats = ['json']

    SucursalController(){
        super(Sucursal)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Sucursal.where {}
        params.sort = params.sort ?:'nombre'
        params.order = params.order ?:'desc'
        if(params.activas){
            query = query.where {activa == true}
        }
        return query.list(params)
    }

    def otrosAlmacenes() {
        def sucursal = AppConfig.first().sucursal
        def almacenes = Sucursal.where{ almacen == true && activa == true && id != sucursal.id}.list()
        respond almacenes
    }
}
