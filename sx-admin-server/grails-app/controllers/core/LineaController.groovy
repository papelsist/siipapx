package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
//@Secured(["hasRole('ROLE_ADMIN')"])
class LineaController extends RestfulController{

    static responseFormats = ['json']

    LineaController() {
        super(Linea)
    }

    protected List listAllResources(Map params) {
        params.max = 500
        resource.list()
    }


}
