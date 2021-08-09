package sx.cxc


import groovy.util.logging.Slf4j



import grails.compiler.GrailsCompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.model.PersistentEntity


import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.api.core.ApiFuture


import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.Cliente
import sx.core.ClienteCredito
import sx.cloud.FirebaseService


@Slf4j
@GrailsCompileStatic
class AplicacionDeCobroListenerService {

	FirebaseService firebaseService

	String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof AplicacionDeCobro ) {
            return ((AplicacionDeCobro) event.entityObject).id
        }
        return null
    }

    AplicacionDeCobro getAplicacion(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof AplicacionDeCobro ) {
            return (AplicacionDeCobro) event.entityObject
        }
        return null
    }

    void afterInsert(PostInsertEvent event) {   
        AplicacionDeCobro aplicacion = getAplicacion(event)
        if ( aplicacion ) {
            Cliente.withNewSession {
                Cliente cliente = Cliente.get(aplicacion.cobro.cliente.id)
                log.info('{} {} : {}', event.eventType.name(), event.entity.name, cliente.nombre)
                updateFirebase(cliente)
            }
        }
    }

    void afterDelete(PostDeleteEvent event) {   
        AplicacionDeCobro aplicacion = getAplicacion(event)
        if ( aplicacion ) {
            Cliente.withNewSession {
                Cliente cliente = Cliente.get(aplicacion.cobro.cliente.id)
                log.info('{} {} : {}', event.eventType.name(), event.entity.name, cliente.nombre)
                updateFirebase(cliente)
            }
        }
    }

   	void updateFirebase(Cliente cliente) {
        if(cliente.credito) {
            ClienteCredito credito = cliente.credito
            log.info('Actualizando saldo del cliente: {}', cliente.nombre, credito.saldo)
        }
    }


}
