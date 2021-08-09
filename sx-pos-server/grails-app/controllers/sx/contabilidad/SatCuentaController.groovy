package sx.contabilidad

import grails.rest.RestfulController

class SatCuentaController extends RestfulController{

    static responseFormats = ['json']

    public SatCuentaController(){
        super(SatCuenta)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 1200
        def query = SatCuenta.where {}

        if(params.nivel){
            query = query.where { nivel == params.nivel}
        }

        if(params.term){
            def search = params.term + '%'
            query = query.where { codigo =~ search || nombre =~ search}
            params.max = 100
            return query.list(params)
        }

        return query.list(params)
    }


}
