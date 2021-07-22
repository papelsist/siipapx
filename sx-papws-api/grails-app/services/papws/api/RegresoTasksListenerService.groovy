package papws.api

import com.google.cloud.Timestamp
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.FirestoreException
import com.google.cloud.firestore.ListenerRegistration
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import javax.annotation.Nullable
import javax.annotation.PreDestroy

import static com.google.cloud.firestore.DocumentChange.Type.ADDED
import static com.google.cloud.firestore.DocumentChange.Type.MODIFIED
import static com.google.cloud.firestore.DocumentChange.Type.REMOVED

import sx.core.AppConfig
import sx.core.Sucursal
import sx.core.Venta

@Slf4j
@Transactional
class RegresoTasksListenerService implements  ApplicationListener<ContextRefreshedEvent>, EventListener<QuerySnapshot> {
  static lazyInit = false

  ListenerRegistration registration
  CloudService cloudService
  Sucursal sucursal
  String tasksCollection = "tasks_cc"

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start()
  }

  // @PostConstruct
  void start() {
    // this.validateSucursal()
    Sucursal sucursal = getSucursal()
    log.info('Inicializando listener par {} de la sucursal: {}', this.tasksCollection, sucursal.nombre)
    this.registration = this.cloudService
      .getFirestore()
      .collection(tasksCollection)
      .whereEqualTo('sucursalId', sucursal.id)
      .whereEqualTo('tipo', 'REGRESAR_PEDIDO')
      .whereEqualTo('replicado', null)
      .limit(10)
      .addSnapshotListener(this)
  }

  @PreDestroy
  void stop() {
    if(registration) {
      registration.remove()
      log.info('Firbase listener for collection: {} has been removed' , this.tasksCollection)
    }
  }

  @Override
  void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException error) {
    if(error) {
      String msg = ExceptionUtils.getRootCauseMessage(error)
      log.error("Error: {}", msg, error)
      return
    }
    snapshots.getDocumentChanges().each { DocumentChange dc ->
      QueryDocumentSnapshot snapshot = dc.getDocument()
      Map<String, Object> data = snapshot.getData()
      log.info('Callcenter Task:  {} : {}', snapshot.id, dc.type)
      switch (dc.type) {
        case ADDED:
        case MODIFIED:
          try{
            log.info('Regresando pedido:{} al CALLCENTER ', data.pedidoFolio)
            regresarPedido(data, snapshot.getId())

          } catch (Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error regresando pedido al callcenter')
            log.error("Message:{}", message)
            Map errorData = [
              entidad: 'Venta',
              task: 'Regresar al Callcenter',
              collection: 'siipap-cc-tasks',
              documentId: snapshot.id
              // comentario: "Error importando pedido ${folio} de ${data.sucursal}",
              // message: message,
              // exception: ExceptionUtils.getRootCause(ex).class.name
            ]
            cloudService.logError(errorData)

          }
          break

        case REMOVED:
          break
      }
    }
  }

  @Transactional
  void regresarPedido(Map<String, Object> data, String documentId){

    def ventaId = data.ventaId
    def pedidoId = data.pedidoId

    Venta venta = Venta.get(ventaId)
    if(venta && validarVenta(venta, ventaId)) {
      log.info("Eliminando venta ")
      venta.delete flush: true
      actualizarPedido(pedidoId)
      actualizarTarea(documentId)
    }
  }

  private actualizarPedido(String pedidoId) {
    DocumentSnapshot snap = fetchPedido(pedidoId)
    if(snap.exists()) {
      Map pedido = snap.getData()
      if(pedido.status == 'CERRADO') {
        DocumentReference ref = snap.reference
        Map changes = [
          status: 'COTIZACION',
          regreso: [
            dateCreated: Timestamp.now(),
            comentario: 'REGRESO AL CALLCENTER',
            solicito: data.solicito,
            autorizo: data.autorizo,
            autorizoUid: data.autorizoUid
          ],
          cierre: [
            replicado: null
          ]
        ]
        def res = ref.update(changes)
          .get()
          .getUpdateTime()
        log.info('Pedido actualizado: {}', res)
      }
    }
  }

  private Timestamp actualizarTarea(String documentId) {
    // Actualizando tarea en firebase
    return this.cloudService.getFirestore()
      .collection(this.tasksCollection)
      .document(documentId)
      .update([replicado: Timestamp.now()])
      .get()
      .getUpdateTime()
  }

  boolean validarVenta(Venta venta){
    if(venta.callcenter == false) {
      log.info('La venta: {} no es de CALLCENTER' , venta.id)
      return false
    }
    if(venta.cuentaPorCobrar) {
      log.info('La venta: {} ya está facturada', venta.id)
      return false
    }
    return true
  }

  DocumentSnapshot fetchPedido(String pedidoId) {
    return this.cloudService
      .getFirestore()
      .document("pedidos/${pedidoId}")
      .get()
      .get()
  }


  Sucursal getSucursal() {
    if(sucursal == null) {
      this.sucursal = AppConfig.first().sucursal
    }
    return this.sucursal
  }

}

