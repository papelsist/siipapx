package sx.cloud


import groovy.util.logging.Slf4j

import org.springframework.scheduling.annotation.Scheduled

import grails.gorm.transactions.Transactional
import grails.compiler.GrailsCompileStatic
import grails.util.Environment

import com.google.cloud.firestore.*
import com.google.firebase.cloud.FirestoreClient
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutures

import org.apache.commons.lang3.exception.ExceptionUtils


import sx.core.Cliente
import sx.audit.Audit

@Slf4j
@GrailsCompileStatic
@Transactional
class LxClienteService {

    String collectionName = 'clientes'

    FirebaseService firebaseService

    def push(Cliente cliente) {
        LxCliente xp = new LxCliente(cliente)
        ApiFuture<WriteResult> result = getCollection()
            .document(xp.id)
            .set(xp.toMap())
        def updateTime = result.get().getUpdateTime().toDate()
        log.debug("{} Published succesful at time : {} " , xp.nombre, updateTime)
        logAudit(xp.id, "UPDATE", "${xp.clave} UPDATED IN FIREBASE", 1, updateTime)
        return updateTime
    }

    

    CollectionReference getCollection() {
        return firebaseService.getFirestore().collection(collectionName)
    }

    /*
    @Transactional(readOnly = true)
    List<LxProducto> findAllProductos() {
        return  Producto.list(fetch: [linea: 'join', marca: 'join', clase: 'join', productoSat: 'join', unidadSat:'join'], 
            sort: 'clave', order: 'asc', max: 5000)
            .collect { Producto prod -> new LxProducto(prod)}
    }
    */
    

    Audit logAudit(String id, String event, String message, int registros, Date updateTime = null) {
        Audit.withNewSession {
            Audit alog = new Audit(
                name: 'LxCliente',
                persistedObjectId: id,
                source: 'OFICINAS',
                target: 'FIREBASE',
                tableName: 'Cliente',
                eventName: event,
                message: message,
                dateReplicated: updateTime
            )
            alog.save failOnError: true, flush: true
        }
    }

    

}
