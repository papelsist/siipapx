package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ProductoController extends RestfulController{

    static responseFormats = ['json']

    public ProductoController(){
        super(Producto)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Producto.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'



        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { clave =~ search || descripcion =~ search}
            query = query.where {deLinea == true }
            query = query.where {activo == true }
        }

        if(params.activos){
            query = query.where {activo == true}
        }

        if(params.deLinea) {
            query = query.where {deLinea == true}
        }
        return query.list(params)
    }

    

}
