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
        def query = Linea.where {}
        params.sort = params.sort ?:'linea'
        params.order = params.order ?:'asc'
        params.max = 1000
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { linea =~ search }
        }
        return query.list(params)
    }


}
