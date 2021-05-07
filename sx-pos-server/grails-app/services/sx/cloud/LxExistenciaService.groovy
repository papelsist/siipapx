package sx.cloud

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.WriteResult
import grails.events.annotation.Subscriber
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import sx.core.Existencia

@Slf4j
class LxExistenciaService {

  FirebaseService firebaseService

  String getId(AbstractPersistenceEvent event) {
    if (event.entityObject instanceof Existencia) {
      return ((Existencia) event.entityObject).id
    }
    null
  }

  Existencia getExistencia(AbstractPersistenceEvent event) {
    if (event.entityObject instanceof Existencia) {
      return (Existencia) event.entityObject
    }
    null
  }

  void afterInsert(PostInsertEvent event) {
    Existencia exis = getExistencia(event)
    if (exis) {
      log.debug('{} {} Exis: {} ({})', event.eventType.name(), event.entity.name, exis.clave, exis.sucursalNombre)
      String id = exis.producto.id
      updateFirebase(exis)
    }
  }

  // @Subscriber
  void afterUpdate(PostUpdateEvent event) {
    Existencia exis = getExistencia(event)
    if (exis) {
      log.debug('{} {} Exis: {} ({})', event.eventType.name(), event.entity.name, exis.clave, exis.sucursalNombre)
      String id = exis.producto.id
      updateFirebase(exis)
    }
  }

  void updateFirebase(Existencia exis) {
    try {
      String nombre = exis.sucursal.nombre == 'CALLE 4' ? 'CALLE4' : exis.sucursal.nombre
      Map data = [
        almacen          : nombre,
        cantidad         : exis.cantidad as Long,
        recorte          : exis.recorte as Long,
        recorteComentario: exis.recorteComentario,
        lastUpdated      : exis.lastUpdated
      ]
      String id = exis.producto.id
      String collection = 'existencias'

      DocumentReference docRef = firebaseService
        .getFirestore()
        .document("${collection}/${id}")

      DocumentSnapshot snapShot = docRef.get().get()

      ApiFuture<WriteResult> result = null

      if (!snapShot.exists()) {

        Map<String, Object> exist = toFirebaseMap(exis)
        docRef.set(exist)

        result = docRef
          .collection('almacenes')
          .document(data.almacen)
          .set(data)
      } else {

        result = docRef
          .collection('almacenes')
          .document(data.almacen)
          .set(data)
        // return null
      }

      def updateTime = result.get()
        .getUpdateTime()
        .toDate()
        .format('dd/MM/yyyy')
      log.debug('{}/{}/almacenes/{} ({}) UPDATED at : {}', collection, id, data.almacen, exis.clave, updateTime)
    }

    catch (Exception ex) {
      def msg = ExceptionUtils.getRootCauseMessage(ex)
      log.error('Error actualizando firebase , Msg: {}', msg)
    }
  }

  Map toFirebaseMap(Existencia exis) {
    Map<String, Object> exist = [
      clave      : exis.producto.clave,
      descripcion: exis.producto.descripcion,
      ejercicio  : exis.anio as Integer,
      mes        : exis.mes as Integer
    ]
  }


}
