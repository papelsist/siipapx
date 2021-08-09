package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SocioController extends RestfulController{

    static responseFormats = ['json']

    public SocioController(){
        super(Socio)
    }

    @Override
    protected List listAllResources(Map params) {
        log.info('List: {}', params)
        def query = Socio.where {}
        params.sort = params.sort ?:'clave'
        params.order = params.order ?:'desc'
        params.max = params.max ?: 30
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombre =~ search }
        }
        return query.list(params)
    }

    

}
