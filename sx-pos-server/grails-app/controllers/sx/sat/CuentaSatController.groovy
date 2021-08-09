package sx.sat


import grails.rest.*
import grails.converters.*

class CuentaSatController extends RestfulController {
    static responseFormats = ['json', 'xml']
    CuentaSatController() {
        super(CuentaSat)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = CuentaSat.where {}
        params.sort = params.sort ?:'nombre'
        params.order = params.order ?:'desc'

        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombre =~ search }
        }
        return query.list(params)
    }
}
