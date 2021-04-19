package papws.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import java.text.SimpleDateFormat
import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import groovy.util.logging.Slf4j

import grails.util.Environment
import grails.gorm.transactions.Transactional



import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.FirestoreException
import com.google.cloud.firestore.ListenerRegistration
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.SetOptions


import static com.google.cloud.firestore.DocumentChange.Type.ADDED
import static com.google.cloud.firestore.DocumentChange.Type.MODIFIED
import static com.google.cloud.firestore.DocumentChange.Type.REMOVED

import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.AppConfig
import sx.core.Sucursal
import sx.core.Cliente
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco
import sx.cxc.SolicitudDeDeposito
import sx.cxc.Cobro
import sx.cxc.CobroTransferencia
import sx.cxc.CobroDeposito


import sx.core.Sucursal

/**
 * Importa Solicitudes desde Firebase - Firestore en PapelWS
 */
@Slf4j
class ImportadorDeSolicitudesService implements  EventListener<QuerySnapshot>{

  static lazyInit = false

  ListenerRegistration registration
  CloudService cloudService

  private SimpleDateFormat sdf = null

  private String COLLECTION = 'solicitudes'

  // @Value("#{systemProperties['papws.sucursal']}")
  @Value('${papws.sucursal}')
  String sucursal

  /*
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start()
  }

   */


  @PostConstruct
  def start() {
    this.validateSucursal()
    log.info('Inicializando listener par solicitudes AUTORIZADAS para sucursal: {}', this.sucursal)
    this.registration = this.cloudService
      .getFirestore()
      .collection(COLLECTION)
      .whereEqualTo('sucursal', sucursal)
      .whereEqualTo('autorizacion.replicado', null)
      .orderBy('dateCreated', Query.Direction.ASCENDING)
      .limit(10)
      .addSnapshotListener(this)
  }

  private validateSucursal() {
    if(!this.sucursal)
      throw new RuntimeException(
        'Falta indicar la sucursal de operacion:  papws.sucursal Ej: ./gradlew -Dpapws.sucursal=TACUBA bootRun')
  }

  @PreDestroy
  def stop() {
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
      log.info('Document: {} Type: {}', folio, dc.type)
      switch (dc.type) {
        case ADDED:
          try{
            registrarSolicitud(data, snapshot.getId())
          } catch (Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)

            log.error('Error importando solicitud Folio: {}  Sucursal: {} FirebaseId: {}', folio, data.sucursal, snapshot.getId())
            log.error("Message:{}", message)

            Map errorData = [
              entidad: 'SolicituDeDepositos',
              task: 'Importar solicitud autorizada',
              collection: 'solicitudes',
              documentId: snapshot.getId(),
              comentario: "Error importando solicitud ${folio} de ${data.sucursal}",
              message: message,
              exception: ExceptionUtils.getRootCause(ex).class.name
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
  void registrarSolicitud(Map<String, Object> data, String documentId){
    log.info('Registrando solicitud {}', data.folio)
    validarCobroNoExistente(data)
    Sucursal sucursal = AppConfig.first().sucursal
    Cliente cliente = Cliente.get(data.cliente.id)
    Banco banco = Banco.get(data.banco.id)
    CuentaDeBanco cuenta = CuentaDeBanco.get(data.cuenta.id)

    Date fecha = getDateFormat().parse(data.fecha)
    Date fechaDeposito = getDateFormat().parse(data.fechaDeposito)
    Integer folio = data.folio as Integer

    log.info('Sucursal: {}, Cliente: {}', sucursal.nombre, cliente.nombre)
    log.info('Banco: {} Cuenta: {}', banco.nombre, cuenta.descripcion)
    log.info('Fecha: {} Fecha deposito: {}', fecha.format('dd/MM/yyyy'), fechaDeposito.format('dd/MM/yyyy'))

    SolicitudDeDeposito sol = new SolicitudDeDeposito(
      [
        sucursal: sucursal,
        cliente: cliente,
        banco: banco,
        cuenta: cuenta,
        folio: folio,
        total: data.total,
        fecha: fecha,
        fechaDeposito: fechaDeposito,
        referencia: data.referencia,
        comentario: 'AUTORIZADO',
        sw2: data.id,
        createUser: data.solicita,
        updateUser: data.solicita,
        transferencia: data.transferencia > 0 ? data.transferencia : 0.0,
        cheque: data.cheque > 0 ? data.cheque : 0.0,
        efectivo: data.efectivo > 0 ? data.efectivo : 0.0,
        tarjeta: data.tarjeta > 0 ? data.tarjeta : 0.0
      ])


    def valid = sol.validate()

    if( valid ) {
      log.info('Solicigud OK : {}', sol)

      // Cobro
      Cobro cobro = new Cobro()
      cobro.cliente = sol.cliente
      cobro.sucursal = sol.sucursal
      cobro.tipo = 'CON'
      cobro.fecha = sol.fecha
      cobro.importe = sol.total
      cobro.referencia = sol.referencia
      cobro.createUser = 'firebase-bot'
      cobro.updateUser = 'firebase-bot'

      if(sol.transferencia > 0.0 ){

        cobro.formaDePago = 'TRANSFERENCIA'
        CobroTransferencia transf = new CobroTransferencia()
        transf.bancoOrigen = sol.banco
        transf.cuentaDestino = sol.cuenta
        transf.folio = sol.folio
        transf.fechaDeposito = sol.fechaDeposito
        transf.sw2 = 'firebase-bot'
        transf.cobro = cobro
        cobro.transferencia = transf

      }

      if(sol.cheque > 0.0 ){

        cobro.formaDePago = 'DEPOSITO_CHEQUE'
        CobroDeposito depo = new CobroDeposito()
        depo.bancoOrigen = sol.banco
        depo.cuentaDestino = sol.cuenta
        depo.folio = sol.folio
        depo.fechaDeposito = sol.fechaDeposito
        depo.totalCheque = sol.cheque
        depo.sw2 = 'firebase-bot'
        depo.cobro = cobro
        cobro.deposito = depo

      }

      if(sol.efectivo > 0.0) {

        cobro.formaDePago = 'DEPOSITO_EFECTIVO'
        CobroDeposito depo = new CobroDeposito()
        depo.bancoOrigen = sol.banco
        depo.cuentaDestino = sol.cuenta
        depo.folio = sol.folio
        depo.fechaDeposito = sol.fechaDeposito
        depo.totalEfectivo = sol.efectivo
        depo.sw2 = 'firebase-bot'
        depo.cobro = cobro
        cobro.deposito = depo
      }

      cobro.save failOnError:true, flush:true
      sol.cobro = cobro
      sol.save failOnError:true, flush:true

      this.cloudService
        .getFirestore()
        .collection(COLLECTION)
        .document(documentId)
        .set(
          [cobro: cobro.id,
           autorizacion: [replicado: new Date()]
          ], SetOptions.merge())

    } else {
      log.info('Errors: {}', sol.errors)
      return
    }
  }


  private validarCobroNoExistente(Map data) {
    if(data.cobro) {
      String cobroId = data.cobro as String
      Cobro cobro = Cobro.get(cobroId)
      if(cobro)
        throw new RuntimeException("Solicitud: ${data.id} con cobro ya existente: ${cobro.id} ")
    }
  }

  SimpleDateFormat getDateFormat() {
    if(! this.sdf) {
      this.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      sdf.setTimeZone(TimeZone.getTimeZone("CET"))
    }
    return this.sdf
  }


}
