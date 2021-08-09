package sx.cxc

import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.time.DateUtils
import sx.core.Folio
import sx.core.Sucursal
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco

@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 80
        def fechaFinal = new Date()
        Date fechaInicial = fechaFinal - 7

        // log.debug('Buscando solicitudes: {}', params)
        // log.debug('Fecha Inicial: {}', fechaInicial)

        def query = SolicitudDeDeposito.where {}
        
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }


        if(params.pendientes) {
            query = query.where{ cobro == null && fecha >=  fechaInicial }
        }

        if(params.autorizadas) {
            query = query.where{ cobro != null && fecha >= fechaInicial}
        }

        if(params.documento) {
            if(params.documento.isNumber()){
                query = SolicitudDeDeposito.where {folio == params.documento}
            }else{
                query = SolicitudDeDeposito.where {createUser =~ params.documento  && fecha >= fechaInicial}
            }
        }

        return query.list(params)
    }


    

    protected SolicitudDeDeposito saveResource(SolicitudDeDeposito resource) {
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        log.debug('Salvando solicitud: {}', resource)
        if(resource.id == null) {
            def serie = resource.sucursal.nombre
            resource.folio = Folio.nextFolio('SOLICITUDES_DEPOSITO',serie)
        }
        return super.saveResource(resource)
    }

    protected SolicitudDeDeposito updateResource(SolicitudDeDeposito resource) {
        log.debug('Actualizando solicitud con params {}', params)
        log.debug('Fecha deposito: {}', resource.fechaDeposito)
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        resource.comentario = null;
        resource.save flush: true
    }

    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'asc'
        def query = SolicitudDeDeposito.where{}
        if (params.folio) {
            query = query.where { sucursal == sucursal && folio == params.folio}
        } else {
            query = query.where{ sucursal == sucursal && cobro == null}
        }
        respond query.list(params)
    }


    def buscarDuplicada(){
       // log.debug('Buscando posible solicitud duplicada {}',params)

        def bancoOri = Banco.get(params.bancoOri)
            def bancoDes = CuentaDeBanco.get(params.bancoDes)
            def fechaDep = params.fechaDep
            def importe = params.importe

        def duplicada = SolicitudDeDeposito.where{
            total == importe && banco == bancoOri && cuenta == bancoDes && fechaDeposito == fechaDep
        }.find()
         //log.debug('Duplicada: ', duplicada)

        respond duplicada?: ['OK']
    }


    
}

