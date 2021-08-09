package sx.cfdi

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import sx.core.Venta

@Secured("hasRole('ROLE_POS_USER')")
class ComplementoIneController extends RestfulController<ComplementoIne>{

    static responseFormats = ['json']

    ComplementoIneController() {
        super(ComplementoIne);
    }

    def pendientes() {
        params.max = 50
        params.sort = 'fecha'
        params.order = 'asc'
        def query = Venta.where{ventaIne == true &&   facturar == null }
        respond query.list(params)
    }
}
