package sx.cxc

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import sx.core.Cobrador

@Secured("hasRole('CXC_USER')")
class CobradorController extends RestfulController{
    static responseFormats = ['json']

    CobradorController(){
        super(Cobrador)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 500
        return super.listAllResources(params)
    }
}
