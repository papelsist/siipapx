package sx.core

import groovy.util.logging.Slf4j


import grails.gorm.transactions.Transactional


@Transactional
@Slf4j
class ClienteService {


    Cliente updateCliente(Cliente cliente) {
        Cliente target = cliente.save failOnError: true, flush: true
        return target
    }



}
