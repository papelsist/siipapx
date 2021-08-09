package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class CorteCobranzaController extends RestfulController {

    static responseFormats = ['json']

    CorteCobranzaController() {
      super(CorteCobranza)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando cortes con {}', params)
        params.sort = 'corte'
        params.order = 'asc'
        params.max = 100
        def query = CorteCobranza.where {}
        if (params.fecha) {
            Date fecha = Date.parse('dd/MM/yyyy', params.fecha)
            query = query.where {fecha == fecha}
        }
        return query.list(params)
    }

    protected CorteCobranza saveResource(CorteCobranza resource) {
        def username = getPrincipal().username
        resource.createUser = username
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected CorteCobranza updateResource(CorteCobranza resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }


}
