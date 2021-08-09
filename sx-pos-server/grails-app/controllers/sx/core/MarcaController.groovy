package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class MarcaController extends RestfulController{

    static responseFormats = ['json']

    public MarcaController(){
        super(Marca)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 500
        params.sort = 'marca'
        params.order = 'asc'
        return super.listAllResources(params)
    }
}
