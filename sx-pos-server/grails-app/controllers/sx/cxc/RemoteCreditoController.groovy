package sx.cxc

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import sx.core.Cliente

@Secured("hasRole('permitAll')")
class RemoteCreditoController extends  RestfulController{

    static responseFormats = ['json']

    RemoteCreditoController() {
        super(Cliente)
    }

    def actualizarCredito(){
        log.debug('Actualizando....', params)
        respond params
    }


}
