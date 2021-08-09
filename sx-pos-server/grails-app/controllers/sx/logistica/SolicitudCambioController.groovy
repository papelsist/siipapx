package sx.logistica

import com.luxsoft.utils.Periodo
import sx.core.Folio
import sx.core.Audit

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.logistica.ModuloTipo

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SolicitudCambioController extends RestfulController {
    static responseFormats = ['json']
    SolicitudCambioController() {
        super(SolicitudCambio)
    }

    def list() {
       
        println params.fechaInicial

        def elements = SolicitudCambio.findAll("from SolicitudCambio where date(fecha) between date(?) and  date(?)",[params.fechaInicial, params.fechaFinal])
        respond elements
       
    }

    def tipos() {

        println params
        def tipos = ModuloTipo.findAll("select tipo from ModuloTipo where modulo = ? ",[params.modulo])
        
        println tipos
        respond tipos
    }

    def salvar(){
        println "Buscando solicitudes de cambio" //+ getObjectToBind(      
        SolicitudCambio sol = new SolicitudCambio()
        bindData sol, getObjectToBind()
        def serie = sol.sucursal.clave
        sol.folio = Folio.nextFolio('MODIFICACION',serie)
        sol.estado = 'PENDIENTE'
        sol.save(failOnError: true, flush:true)

        def audit = new Audit();

        audit.persistedObjectId = sol.id
        audit.target = 'OFICINAS'
        audit.name = 'SolicitudCambio'
        audit.tableName = 'solicitud_cambio'
        audit.source = sol.sucursal.nombre
        audit.eventName = 'INSERT'

        audit.save(failOnError: true, flush:true)

        respond sol 
    }
}
