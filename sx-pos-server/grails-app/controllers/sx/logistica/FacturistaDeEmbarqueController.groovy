package sx.logistica


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Sucursal

@Secured("ROLE_EMBARQUES_USER")
class FacturistaDeEmbarqueController extends RestfulController {
    
    static responseFormats = ['json']

    FacturistaDeEmbarqueController() {
        super(FacturistaDeEmbarque)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        return FacturistaDeEmbarque.list(params)
    }

    protected FacturistaDeEmbarque saveResource(FacturistaDeEmbarque resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected FacturistaDeEmbarque updateResource(FacturistaDeEmbarque resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

}
