package sx.sat


import grails.rest.*
import grails.converters.*

class BancoSatController extends RestfulController {
    static responseFormats = ['json', 'xml']
    BancoSatController() {
        super(BancoSat)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = BancoSat.where {}
        params.sort = params.sort ?:'nombreCorto'
        params.order = params.order ?:'desc'

        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { nombreCorto =~ search }
        }
        return query.list(params)
    }
}
