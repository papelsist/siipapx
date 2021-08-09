package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("ROLE_COMPRAS_USER")
class ProveedorProductoController extends RestfulController{

    static responseFormats = ['json']

    ProveedorProductoController(){
        super(ProveedorProducto)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 1000
        def query = ProveedorProducto.where {}
        if(params.proveedorId){
            query = query.where{proveedor.id == params.proveedorId}
        }
        if( params.term ){
            def search = '%' + params.term + '%'
            query = query.where { producto.clave =~ search || producto.descripcion =~ search}
        }
        if(params.activos){
            query = query.where {producto.activo == true}
        }
        return query.list(params)
    }


}
