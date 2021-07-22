package sx.cloud


import groovy.util.logging.Slf4j


import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import grails.compiler.GrailsCompileStatic
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.model.PersistentEntity
import grails.util.Environment

import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.api.core.ApiFuture


import org.apache.commons.lang3.exception.ExceptionUtils


import sx.logistica.Envio
// import papws.api.CloudService
import sx.cloud.papws.PedidosService

@Slf4j
@GrailsCompileStatic
// @Transactional
class LxEmbarqueService {

	FirebaseService firebaseService
    PedidosService pedidosService

	String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Envio ) {
            return ((Envio) event.entityObject).id
        }
        null
    }

    Envio getEnvio(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Envio ) {
            return (Envio) event.entityObject
        }
        null
    }

    @Subscriber
    void afterInsert(PostInsertEvent event) {
        Envio envio = getEnvio(event)
        if ( envio ) {
            def emb = envio.embarque
            log.debug('Asignando Facura {}-{} al Embarque: {} ', envio.tipoDocumento, envio.documento, emb.documento)
            if(envio.callcenter) {
                log.debug('Nofificando Callcenter ')
                def changes = [
                    embarqueLog: [
                        embarque: emb.documento,
                        chofer: emb.chofer.nombre,
                        asignado: envio.dateCreated
                    ]
                ]
                if(envio.callcenter) {
                    if(envio.callcenterVersion == 2) {
                        updateCallcenter2(envio.callcenter, changes)
                    } else {
                        updateFirebase(envio.callcenter, changes)
                    }
                }
                
            }
        }
    }

    @Subscriber
    void afterUpdate(PostUpdateEvent event) {
        	
        Envio envio = getEnvio(event)
        if ( envio && envio.callcenter) {
            PersistentEntity pe = event.getEntity()
            org.hibernate.event.spi.PostUpdateEvent he = (org.hibernate.event.spi.PostUpdateEvent)event.getNativeEvent()
            List nameMap = he.getPersister().getPropertyNames() as List
            Object[] oldState = he.getOldState()
            Object[] state = he.getState()
            def dirties = he.getDirtyProperties() as List
            dirties.each { idx ->
                int i = (int)idx
                String property = nameMap.get(i)
                if(property == 'salida') {
                    String newState = state[i]
                    log.debug('Facura {}-{} Salida: {} ', envio.tipoDocumento, envio.documento, newState)
                    def changes = [embarqueLog: [salida: newState]]
                    if(envio.callcenterVersion == 2) {
                        updateCallcenter2(envio.callcenter, changes)
                    } else {

                        updateFirebase(envio.callcenter, changes)
                    }
                }

                if(property == 'recepcion') {
                    
                    String newState = state[i]
                    def recepcion = null
                    if(newState) {
                        recepcion = [
                            arribo: envio.arribo,
                            recepcion: envio.recepcion,
                            recibio: envio.recibio,
                            comentario: envio.comentario
                            ]
                    } 
                    log.debug('Facura {}-{} Recibida: {} ', envio.tipoDocumento, envio.documento, newState)
                    def changes = [embarqueLog: [recepcion: recepcion]]
                    if(envio.callcenterVersion == 2) {
                        updateCallcenter2(envio.callcenter, changes)
                    } else {
                        updateFirebase(envio.callcenter, changes)
                    }
                    
                }
            }
        }
        
    }

    @Subscriber
    void afterDelete(PostDeleteEvent event) {
        Envio envio = getEnvio(event)
        if ( envio ) {
            def emb = envio.embarque
            log.debug('Quitando Facura {}-{} del Embarque: {} ', envio.tipoDocumento, envio.documento, emb.documento)
            if(envio.callcenter) {
                log.debug('Nofificando Callcenter ')
                def changes = [embarqueLog: null]
                if(envio.callcenterVersion == 2) {
                    updateCallcenter2(envio.callcenter, changes)
                } else {
                    updateFirebase(envio.callcenter, changes)
                }
                
            }
        }
    }

    void updateFirebase(String id, Map changes) {
        firebaseService.updateCollection('pedidos_log', id, changes)
    }

    void updateCallcenter2(String id, Map changes) {
        pedidosService.updatePedido(id, changes)
    }


}
