package sx.cajas
 
 
import grails.rest.*
import grails.converters.*
 
import com.luxsoft.utils.Periodo
import sx.core.Folio
import sx.core.Audit
import grails.plugin.springsecurity.annotation.Secured
 
@Secured("ROLE_INVENTARIO_USER")
class CotizacionCajaController extends RestfulController {

   static responseFormats = ['json']

   CotizacionCajaController() {
       super(CotizacionCaja)
   }


    CotizacionCajaService cotizacionCajaService

    def list() {
       //def elements = SolicitudCambio.findAll("from SolicitudCambio where date(fecha) between date(?) and  date(?)",[params.fechaInicial, params.fechaFinal])
       def elements = CotizacionCaja.findAll("from CotizacionCaja")
       respond elements   
   }
 
 
 
   def save(){

      CotizacionCaja caja = new CotizacionCaja()
      bindData caja, getObjectToBind()
      caja.folio = Folio.nextFolio('COTS','CJA')

      println caja

      
      caja.save failOnError:true, flush:true
      respond caja

   }


  def cerrar(CotizacionCaja cotizacionCaja){    
    def producto = cotizacionCajaService.cerrar(cotizacionCaja)
    respond producto
  }



}
