package sx.core

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.inventario.Traslado



@Transactional
class ClienteCreditoService {


    
    ClienteCredito updateCliente(ClienteCredito credito) {
        ClienteCredito target = credito.save failOnError: true, flush: true
        return target

    }

}
