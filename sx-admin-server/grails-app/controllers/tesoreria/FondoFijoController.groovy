package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class FondoFijoController extends RestfulController {

    static responseFormats = ['json']

    FondoFijoController() {
      super(FondoFijo)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'dateCreated'
        params.order = 'asc'
        params.max = 100
        def query = FondoFijo.where {}
        return query.list(params)
    }

    protected FondoFijo saveResource(FondoFijo resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected FondoFijo updateResource(FondoFijo resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }


}
