package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("ROLE_CXC_USER")
class BancoController extends RestfulController {

    static responseFormats = ['json']

    BancoController() {
      super(Banco)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('Buscando bancos: {}', params)
        def query = Banco.where {}
        params.sort = params.sort ?:'nombre'
        params.order = params.order ?:'asc'
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombre =~ search}
        }
        return query.list(params)
    }
}
