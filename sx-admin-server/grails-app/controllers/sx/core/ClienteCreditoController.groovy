package sx.core

import grails.rest.RestfulController
import sx.core.ClienteCredito


class ClienteCreditoController extends RestfulController<ClienteCredito> {

	ClienteCreditoService clienteCreditoService

    ClienteCreditoController(){
        super(ClienteCredito)
    }

    @Override
    protected ClienteCredito updateResource(ClienteCredito resource) {
        log.debug('Actualizando {}', resource)
        return clienteCreditoService.updateCliente(resource)
    }
}
