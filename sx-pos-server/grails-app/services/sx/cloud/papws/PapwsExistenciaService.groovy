/*
 * Copyright (c) 2021 - 2. Ruben Cancino Ramos.
 */
package sx.cloud.papws

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import groovy.util.logging.Slf4j
import sx.core.Existencia

@Slf4j
class PapwsExistenciaService {

  CloudService cloudService

  def pushExistencia(Existencia exis) {
    final String nombre = exis.sucursal.nombre.replaceAll("\\s","")
      .toLowerCase()
    String id = exis.producto.id
    String collection = 'productos'
    DocumentReference docRef = cloudService
      .getFirestore()
      .document("${collection}/${id}")
    DocumentSnapshot snapShot = docRef.get().get()

    if (snapShot.exists()) {
      Map data = [:]
      data["${nombre.toLowerCase()}"] = [
        cantidad         : exis.cantidad as Long,
        recorte          : exis.recorte as Long,
        recorteComentario: exis.recorteComentario,
        lastUpdated      : exis.lastUpdated
      ]
      ApiFuture<WriteResult> result = docRef.set([existencia: data], SetOptions.merge())
      def updateTime = result.get()
        .getUpdateTime()
        .toDate()
        .format('dd/MM/yyyy')
      log.debug('Existencia de {} / {} actualizada: {}', exis.clave, exis.sucursalNombre, updateTime)
    }
  }
}
