package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
//@Secured(["hasRole('ROLE_ADMIN')"])
class ClaseController extends RestfulController{

    static responseFormats = ['json']

    public ClaseController(){
        super(Clase)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 500
        return super.listAllResources(params)
    }

}
