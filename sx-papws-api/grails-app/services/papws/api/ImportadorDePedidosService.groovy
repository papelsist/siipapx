package papws.api


import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.FirestoreException
import com.google.cloud.firestore.ListenerRegistration

import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.SetOptions
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import sx.core.Venta

import javax.annotation.Nullable
import javax.annotation.PreDestroy
import java.text.SimpleDateFormat

import static com.google.cloud.firestore.DocumentChange.Type.ADDED
import static com.google.cloud.firestore.DocumentChange.Type.MODIFIED
import static com.google.cloud.firestore.DocumentChange.Type.REMOVED

import sx.core.AppConfig
import sx.core.Sucursal

@Slf4j
@Transactional
class ImportadorDePedidosService implements  ApplicationListener<ContextRefreshedEvent>, EventListener<QuerySnapshot> {
  static lazyInit = false

  ListenerRegistration registration
  CloudService cloudService

  private SimpleDateFormat sdf = null

  private String COLLECTION = 'pedidos'

  Sucursal sucursal

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start()
  }

  // @PostConstruct
  void start() {
    // this.validateSucursal()
    Sucursal sucursal = getSucursal()
    log.info('Inicializando listener par PEDIDOS CERRADOS  de la sucursal: {}', sucursal.nombre)
    this.registration = this.cloudService
      .getFirestore()
      .collection(COLLECTION)
      .whereEqualTo('appVersion', 2)
      .whereEqualTo('sucursalId', sucursal.id)
      .whereEqualTo('status', 'CERRADO')
      .whereEqualTo('cierre.replicado', null)
      // .orderBy('dateCreated', Query.Direction.ASCENDING)
      .limit(1)
      .addSnapshotListener(this)
  }

  @PreDestroy
  void stop() {
    if(registration) {
      registration.remove()
      log.info('Firbase listener for collection: {} has been removed' , COLLECTION)
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
      Integer folio = snapshot.get('folio') as Integer
      Map<String, Object> data = snapshot.getData()
      log.info('Pedido:  {} : {}', folio, dc.type)
      switch (dc.type) {
        case ADDED:
          try{
            registrarPedido(data, snapshot.getId())
          } catch (Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)

            log.error('Error importando pedido:({}) {} FirebaseId: {}', data.sucursal, folio, snapshot.getId())
            log.error("Message:{}", message)

            Map errorData = [
              entidad: 'Venta',
              task: 'Importar pedido cerrado',
              collection: 'pedidos',
              documentId: snapshot.id
              // comentario: "Error importando pedido ${folio} de ${data.sucursal}",
              // message: message,
              // exception: ExceptionUtils.getRootCause(ex).class.name
            ]
            cloudService.logError(errorData)

          }
          break
        case MODIFIED:
        case REMOVED:
          break
      }
    }
  }

  @Transactional
  void registrarPedido(Map<String, Object> pedido, String documentId){

    log.info('Registrando pedido {}', pedido.folio)
    Venta found = Venta.where{sw2 == pedido.id}.find()
    if(found) {
      log.info("Pedido ya importado Venta Id: {}", found.id)
      return
    }
    if(getSucursal().id != pedido.sucursalId) {
      log.info("Pedido no corresponde a la sucursal: {} esta registrado como sucursal: {}", getSucursal(), pedido.sucursal)
      return
    }
    log.info('Cliente: {}', pedido.nombre)
    log.info('Pedido: ({}) {} Fecha: {}', pedido.sucursal, pedido.folio, pedido.fecha.toDate().format('dd/MM/yyyy'))

    PedidoConverter converter = new PedidoConverter()
    Venta venta = converter.mapToVenta(pedido, AppConfig.first().sucursal)
    boolean valid = venta.validate()
    if(valid) {
      venta = venta.save failOnError: true, flush: true
      this.cloudService
        .getFirestore()
        .collection(COLLECTION)
        .document(pedido.id)
        .set(
          [venta: venta.id,
           cierre: [replicado: new Date()]
          ], SetOptions.merge())
    }
  }


  Sucursal getSucursal() {
    if(sucursal == null) {
      this.sucursal = AppConfig.first().sucursal
    }
    return this.sucursal
  }

}
