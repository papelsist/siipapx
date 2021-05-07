/*
 * Copyright (c) 2021 - 2. Ruben Cancino Ramos Derechos reservados
 */

package sx.core

import com.google.cloud.firestore.SetOptions
import grails.events.annotation.gorm.Listener
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import sx.cloud.LxExistenciaService
import sx.cloud.papws.CloudService

@Slf4j
class ExistenciaListener {

  CloudService cloudService
  LxExistenciaService lxExistenciaService

  @Listener(Existencia)
  void onPreInsertEvent(PreInsertEvent event) {}

  @Listener(Existencia)
  void onPostInsertEvent(PostInsertEvent event) {
    // logFirebase(event)
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
      List<String> dirties = exis.dirtyPropertyNames.findAll {it != 'lastUpdate'}
      List criticalProperties = ['cantidad']
      log.debug("Existencia dirty properties: {}", dirties)
      if(dirties.contains('cantidad') || dirties.contains('recorte')) {
        log.debug('Cantidad/Recorte modificados')
        try {
          logFirebase(exis)
        } catch(Exception ex) {
          String msg = ExceptionUtils.getRootCauseMessage(ex)
          log.error('Error actualizando firebase ' + msg, ex)
        }

      }
    }
  }

  void logFirebase(Existencia exis) {
    log.debug('Actualizando firestore Prod: {} cant:{} recorte: {}', exis.clave, exis.cantidad, exis.recorte)
    Map<String,Object> almacen = [
      cantidad: exis.cantidad.toDouble(),
      recorte: exis.recorte.toDouble(),
      recorteComentario:exis.recorteComentario,
      lastUpdated: new Date()
    ]
    Map<String,Object> changes = [:]
    String key = exis.sucursal.nombre == 'CALLE 4' ? 'calle4' : exis.sucursal.nombre.toLowerCase()
    changes.put(key, almacen)
    String productoId = exis.producto.id
    this.cloudService
      .getFirestore()
      .collection('productos')
      .document(productoId)
      .set([existencia: changes], SetOptions.merge())
    log.info('ProductoId: {} changes: {}', productoId, changes)
    this.lxExistenciaService.updateFirebase(exis)
  }




  void logProperty(String property, ClienteCredito b, Map<String, Map<String,Object>> changes) {
    if(b.isDirty(property)) {
      def current = b[property]
      def original = b.getPersistentValue(property)
      if (current != original) {
        changes.put(property, ['original': original, 'curent': current])
      }
    }
  }


}
