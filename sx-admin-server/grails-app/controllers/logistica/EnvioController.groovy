package sx.logistica

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Sucursal
import sx.core.Venta
import grails.transaction.Transactional



@Secured("ROLE_INVENTARIO_USER")
class EnvioController extends RestfulController {
    
    static responseFormats = ['json']
    
    def reporteService

    EnvioController() {
        super(Envio)
    }

    

    protected Envio updateResource(Envio resource) {
        
        return super.updateResource(resource)
    }


    def buscarPartidasParaEnvio(EnvioPartidasSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = CondicionDeEnvio.where{
            venta.sucursal == command.sucursal && venta.documento == command.documento && venta.fecha == command.fecha
        }
        CondicionDeEnvio res = q.find()
        if (res == null) {
            notFound()
            return
        }
        // println 'Condicion encontrada: ' + res.venta
        def venta = res.venta
        respond venta.partidas
    }

    


}

class EnvioPartidasSearchCommand {
    
    String tipo
    Date fecha
    Sucursal sucursal
    Long documento

    String toString(){
        "Tipo:$tipo Docto:$documento Fecha:${fecha.format('dd/MM/yyyy')} Sucursal:$sucursal"
    }
}

