package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ProveedorController extends RestfulController{

    static responseFormats = ['json']

    public ProveedorController(){
        super(Proveedor)
    }

    @Override
    protected List listAllResources(Map params) {

        def query = Proveedor.where { tipo == 'COMPRAS' && activo == true}
        params.max = 50

        if(params.nacional){
            query = query.where {nacional == params.nacional}
        }
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombre =~ search}
            params.max = 50
            return query.list(params)
        }
        return query.list(params)
    }

}
