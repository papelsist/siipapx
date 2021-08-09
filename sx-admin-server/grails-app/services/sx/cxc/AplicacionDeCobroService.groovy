package sx.cxc

import grails.gorm.transactions.Transactional

@Transactional
class AplicacionDeCobroService {

    AplicacionDeCobro save(AplicacionDeCobro aplicacion) {
        aplicacion.formaDePago = aplicacion.cobro.formaDePago
    }
}
