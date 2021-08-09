package cxc

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import sx.cxc.DespachoDeCobranza

@Secured("hasRole('CXC_USER')")
class DespachoDeCobranzaController extends RestfulController<DespachoDeCobranza>{

    DespachoDeCobranzaController(){
        super(DespachoDeCobranza)
    }

    @Override
    protected List<DespachoDeCobranza> listAllResources(Map params) {
        params.max=500
        return super.listAllResources(params)
    }
}
