package sx.logistica


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Sucursal

@Secured("ROLE_EMBARQUES_USER")
class ChoferController extends RestfulController {
    
    static responseFormats = ['json']

    ChoferController() {
        super(Chofer)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Chofer.where {}
        params.sort = params.sort ?:'nombre'
        params.order = params.order ?:'asc'
        params.max = params.max ?: 200
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombre =~ search }
        }
        return query.list(params)
    }

    protected Chofer saveResource(Chofer resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Chofer updateResource(Chofer resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

}
