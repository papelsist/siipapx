package sx.tesoreria


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("ROLE_CXC_USER")
class CuentaDeBancoController extends RestfulController {

    static responseFormats = ['json']

    CuentaDeBancoController() {
        super(CuentaDeBanco)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'descripcion'
        params.order = 'asc'
        params.max = 100
        def query = CuentaDeBanco.where {}
        if(params.disponibleEnVenta){
            query = query.where {disponibleEnVenta ==  true}
        }
        if(params.activa) {
            query = query.where {activo ==  true}
        }
        return query.list(params)
    }
}
