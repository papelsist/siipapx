package sx.compras

import grails.rest.RestfulController

class RecepcionDeCompraDetController extends RestfulController{

    static responseFormats = ['json']

    RecepcionDeCompraDetController(){
        super(RecepcionDeCompraDet)
    }


    @Override
    protected List listAllResources(Map params) {
        def query = RecepcionDeCompraDet.where {}
        if(params.recepcionDeCompraId){
            query = query.where{recepcion.id == params.recepcionDeCompraId}
            return query.list()
        }
        return query.list(params)
    }


}
