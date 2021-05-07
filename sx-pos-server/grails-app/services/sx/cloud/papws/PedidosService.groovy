package sx.cloud.papws

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.core.Venta
import sx.cxc.CuentaPorCobrar

@Slf4j
class PedidosService {
  CloudService cloudService

  void updatePedido(String id, Map changes) {
    String collection = 'pedidos'
    try {
      ApiFuture<WriteResult> result = this.cloudService.getFirestore()
        .collection(collection)
        .document(id)
        .set(changes, SetOptions.merge())
      def updateTime = result.get().getUpdateTime().toDate().format('dd/MM/yyyy')
      log.info('Pedido {} actualizado at: {}', id, updateTime)
    }
    catch(Exception ex) {
      def msg = ExceptionUtils.getRootCauseMessage(ex)
      log.error('Error actualizando {} DocId: {} , Msg: {}', collection, id, msg)
    }
  }

  void mandarFacturar(Venta venta) {
    if(venta.callcenterVersion == 2) {
      Map update = [status: 'POR_FACTURAR', atencion: [
        atiende: venta.facturarUsuario,
        facturable: venta.facturar,
        atendio: new Date()
      ]]
      log.info('Actualizando pedido {} status: {} en firestore', venta.documento, update.status)
      this.updatePedido(venta.sw2, update)
    }
  }

  void registrarPuesto(Venta venta, String usuario) {
    if(venta.callcenterVersion == 2) {
      try {
        def puesto = venta.puesto
        Map changes = [puesto: null]
        if(puesto) {
          changes = [puesto: [fecha: puesto, usuario: usuario]]
        }
        this.updatePedido(venta.sw2, changes)
      } catch (Exception ex) {
        String message = ExceptionUtils.getRootCauseMessage(ex)
        log.error('Error actualizando PAPWS Firestore ' + message, ExceptionUtils.getRootCauseMessage(ex))
      }

    }
  }

  void notificarFacturacion(Venta venta ) {
    if(venta.callcenterVersion == 2) {
      CuentaPorCobrar cxc = venta.cuentaPorCobrar
      String serie = venta.tipo
      String folio = cxc.documento.toString()
      Map<String,Object> changes = [
        status: 'FACTURADO',
        facturacion: [
          serie: serie,
          folio: folio,
          creado: cxc.dateCreated
        ]
      ]
      this.updatePedido(venta.sw2, changes)
    }
  }

  void notificarTimbrado(Venta venta, Cfdi cfdi) {
    if(venta.callcenterVersion == 2) {
      Map<String,Object> changes = [
        status: 'FACTURADO_TIMBRADO',
        factura: [
          serie: cfdi.serie,
          folio: cfdi.folio,
          uuid: cfdi.uuid,
          cfdi: cfdi.id,
          createUser: 'ND',
          updateUser: 'ND']
      ]
      this.updatePedido(venta.sw2, changes)

    }

  }
}
