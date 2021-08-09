package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class MorrallaController extends RestfulController {

    static responseFormats = ['json']

    MorrallaController() {
      super(Morralla)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'fecha'
        params.order = 'desc'
        params.max = 100
        def query = Morralla.where {}
        return query.list(params)
    }

    protected Morralla saveResource(Morralla resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        if (resource.tipo == 'SALIDA'){
            resource.importe = resource.importe.abs() * -1
        }
        return super.saveResource(resource)
    }

    protected Morralla updateResource(Morralla resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }


}
