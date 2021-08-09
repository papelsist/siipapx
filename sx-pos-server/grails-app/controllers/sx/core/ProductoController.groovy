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
        params.sort = params.sort ?:'clave'
        params.order = params.order ?:'desc'
        params.max = 50
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { clave =~ search || descripcion =~ search}
        }
        if(params.activos){
            query = query.where {activo == true}
        }
        if(params.deLinea) {
            query = query.where {deLinea == true}
        }
        def list = query.list(params)
        return list
    }

    

}
