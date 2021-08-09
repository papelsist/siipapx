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

        //params.sort = 'lastUpdated'
        //params.order = 'desc'
        params.max = 100
        def query = Banco.where {}
   
        return query.list(params)
    }
}
