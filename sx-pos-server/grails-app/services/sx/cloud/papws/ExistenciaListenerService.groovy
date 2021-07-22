package sx.cloud.papws

import grails.events.annotation.gorm.Listener
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

import sx.core.Existencia

/**
 * Detecta cambios en la existencia para propagarlos a Firebase
 * 
 **/
@Slf4j
class ExistenciaListenerService {

  PapwsExistenciaService papwsExistenciaService

  @Listener(Existencia)
  void onPreInsertEvent(PreInsertEvent event) {
    preLogExistencia(event)
  }

  @Listener(Existencia)
  void onPreUpdateEvent(PreUpdateEvent event) {
    preLogExistencia(event)
  }

  @Listener(Existencia)
  void onPostUpdateEvent(PostUpdateEvent event) {}
  

  private void preLogExistencia(AbstractPersistenceEvent event) {
    if (event.entityObject instanceof Existencia) {
      Existencia exis = event.entityObject as Existencia
      def dirties = exis.dirtyPropertyNames
      log.debug("Exis {} Dirty properties: {}", exis.clave, dirties)
      if(exis.isDirty('cantidad') || exis.isDirty('recorte') || exis.isDirty('recorteComentario') ) {
        this.papwsExistenciaService.pushExistencia(exis)
      }
    }
  }
}