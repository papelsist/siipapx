package sx.contabilidad

import grails.rest.RestfulController

class SatBancoController extends RestfulController{

    static responseFormats = ['json']

    public SatBancoController(){
        super(SatBanco)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = SatBanco.where {}
        params.max = 300
        if(params.term){
            def search = params.term + '%'
            query = query.where { nombreCorto =~ search || razonSocial =~ search}
            params.max = 100
            return query.list(params)
        }
        return query.list(params)
    }
}
