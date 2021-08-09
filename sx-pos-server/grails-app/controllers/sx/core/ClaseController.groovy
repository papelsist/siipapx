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
        def query = Clase.where {}
        params.sort = params.sort ?:'clase'
        params.order = params.order ?:'asc'
        params.max = 1000
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { clase =~ search }
        }
        return query.list(params)
    }

}
