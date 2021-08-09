package sx.cloud


import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Scheduled
import grails.gorm.transactions.Transactional
import grails.compiler.GrailsCompileStatic
import grails.util.Environment
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutures
import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.Venta


@Slf4j
// @Transactional
class LxPedidoService {

    FirebaseService firebaseService


    void updatePedido(String id, Map changes) {
        firebaseService.updateCollection('pedidos', id, changes)
    }

    void updateLog(String id, Map changes) {
        firebaseService.updateCollection('pedidos_log', id, changes)
    }


}
