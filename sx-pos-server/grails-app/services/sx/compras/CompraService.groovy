package sx.compras

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional

@Transactional
class CompraService {

    @Subscriber
    def onSaveCom(RecepcionDeCompra com) {
        log.debug('Detectando modificacion de com {}', com.documento)
        Compra.withNewSession {
            Compra compra = Compra.get(com.compra.id)
            compra.refresh()

                if(compra.partidas.find{it.getPorRecibir()> 0.0 } == null){
                compra.pendiente=false
            }
            else{
                compra.pendiente=true
            }
            compra.save flush: true
            log.debug('Status de compra {} actualizado', compra.folio)
        }
    }
}
