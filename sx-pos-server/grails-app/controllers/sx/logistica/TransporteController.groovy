package sx.logistica


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured



@Secured("ROLE_EMBARQUES_USER")
class TransporteController extends RestfulController {
    
    static responseFormats = ['json']

    TransporteController() {
        super(Transporte)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        return Transporte.list(params)
    }

    protected Transporte saveResource(Transporte resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Transporte updateResource(Transporte resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

}
