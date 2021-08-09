package sx.cxc

import groovy.util.logging.Slf4j
import groovy.transform.ToString


import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.utils.Periodo
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Sucursal
import sx.reports.ReportService
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.SolicitudDeDepositoService

@Slf4j
@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController<SolicitudDeDeposito>{

    static responseFormats = ['json']

    ReportService reportService

    SolicitudDeDepositoService solicitudDeDepositoService;

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = params.registros ?:1000
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = SolicitudDeDeposito.where {}
        if (params.cartera) {
            def cartera = params.cartera
            if(cartera == 'CRE'){
                query = query.where { tipo == 'CRE' || tipo == 'CHE' || tipo == 'JUR'}
            } else {
                query = query.where { tipo == cartera}
            }

        }
        if (params.sucursal) {
            Sucursal suc = Sucursal.get(params.sucursal)
            query = query.where { sucursal == suc}
        }
        if (params.pendientes) {
            if (params.boolean('pendientes')){
                // log.debug('Buscando solicitudes pendientes')
                query = query.where {cobro == null && cancelacion == null}
            } else {
                // log.debug('Buscando solicitudes atendidas')
                query = query.where {cobro != null}
            }
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { folio == params.term.toInteger() }
            } else {
                query = query.where { cliente.nombre =~ search || banco.nombre =~ search  }
            }
        }
        return query.list(params)
    }

    def pendientes() {
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'asc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario == null && cancelacion == null
        }
        def list = query.list(params)
        respond list
    }

    def autorizadas(SolicitudFilter filter) {
        log.info('Solicitudes autorizdas {}', params)
        params.max = 500
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro != null
        }
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }

        if( params.periodo) {
            Periodo periodo = new Periodo()
            bindData(periodo, params.periodo)
            // log.debug('Periodo: {}', periodo)
            query = query.where{fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}

        }

        if( params.folio && params.folio.isInteger()){
            // log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            // log.debug('Buscando por total: {}', params.total.toBigDecimal())
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            // log.debug('Buscando por fecha deposito: {}', filter.fechaDeposito)
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }
        if(filter.fechaCobranza) {
            // log.debug('Buscando por fecha cobranza: {}', filter.fechaCobranza)
            query = query.where { cobro.primeraAplicacion == filter.fechaCobranza }
        }

        if(filter.fechaCobranza) {
            log.debug('Buscando por fecha cobranza: {}', filter.fechaCobranza)
            query = query.where { cobro.primeraAplicacion == filter.fechaCobranza }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }
        if (params.banco) {
            String search = '%' + params.banco+ '%'
            query = query.where { cuenta.descripcion =~ search  }
        }

        if (params.tipo) {
            String tipo = params.tipo
            query = query.where { cobro.tipo =~ tipo }
        }

        if (params.formaDePago) {
            String search = '%' + params.formaDePago+ '%'
            query = query.where { cobro.formaDePago =~ search  }
        }


        def list = query.list(params)
        respond list
    }

    def transito(SolicitudFilter filter) {
        // log.debug('Buscando solicitudes transito {}', params)
        // log.debug('Filter: {}', filter)
        params.max = 50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario != null
        }

        if( params.folio && params.folio.isInteger()){
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }

        def list = query.list(params)
        respond list
    }

    def canceladas(SolicitudFilter filter) {
        //log.debug('Buscando solicitudes transito {}', params)
        // log.debug('Filter: {}', filter)
        params.max = 50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cancelacion!= null
        }

        if( params.folio && params.folio.isInteger()){
            // log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            // log.debug('Buscando por total: ', params.total.toBigDecimal())
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            // log.debug('Buscando por fecha: {}', filter.fechaDeposito)
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }

        if(filter.fechaCobranza) {
            // log.debug('Buscando por fecha cobranza: {}', filter.fechaCobranza)
            query = query.where { cobro.primeraAplicacion == filter.fechaCobranza }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }

        def list = query.list(params)
        respond list
    }

    @Transactional
    def autorizar() {
        SolicitudDeDeposito sol = SolicitudDeDeposito.get(params.id)
        def res = solicitudDeDepositoService.autorizar(sol)
        respond res;
    }

    @Transactional
    def posponer() {
        SolicitudDeDeposito sol = SolicitudDeDeposito.get(params.id)
        sol.sw2 = new Date().toString();
        sol.save flush:true
        respond sol
    }

    @Transactional
    def rechazar() {
        SolicitudDeDeposito sol = SolicitudDeDeposito.get(params.id)
        sol.comentario = params.comentario
        updateResource sol
        respond sol
    }

    @Transactional
    def cancelar() {
        SolicitudDeDeposito sol = SolicitudDeDeposito.get(params.id)
        sol.cancelacionComentario = params.comentario
        sol.cancelacion = new Date()
        sol.save flush: true
        respond sol
    }


    @Override
    protected SolicitudDeDeposito createResource() {
        SolicitudDeDeposito sol = new SolicitudDeDeposito()
        bindData sol, getObjectToBind()
        sol.sucursal = Sucursal.where { clave == 1}.find()
        sol.fecha = new Date()
        return sol
    }

    protected SolicitudDeDeposito saveResource(SolicitudDeDeposito resource) {
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        // log.debug('Salvando solicitud: {}', resource)
        if(resource.id == null) {
            def serie = resource.sucursal.nombre
            resource.folio = Folio.nextFolio('SOLICITUDES_DEPOSITO',serie)
        }
        return super.saveResource(resource)
    }

    @Override
    protected SolicitudDeDeposito updateResource(SolicitudDeDeposito resource) {
        // log.debug('Actualizando solicitud: {} ', resource)
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        return super.updateResource(resource)
    }

    def buscarDuplicada(SolicitudDeDeposito instance){
        //log.debug('Buscando posible solicitud duplicada {}', instance.folio)

        def duplicada = SolicitudDeDeposito.where{
            id!= instance.id && total == instance.total && banco == instance.banco && cuenta == instance.cuenta && fechaDeposito == instance.fechaDeposito
        }.find()
        // log.debug('Duplicada: ', duplicada)

        respond duplicada?: ['OK']
    }

    def buscarPosibleDuplicadaCallcenter(PosibleDuplicadoCommand command) {
        def duplicada = SolicitudDeDeposito.where{
            total == command.total && 
            banco.id == command.banco && 
            cuenta.id == command.cuenta && 
            fechaDeposito == command.fechaDeposito
        }.find()
        
        if(!duplicada) {
            notFound()
            return
        }
        
        respond duplicada
    }

    def ingreso(SolicitudDeDeposito sol) {
        assert sol.cobro, 'Solicitud no autoriada no puede registrar ingreso'
        def res = solicitudDeDepositoService.registrarIngreso(sol.cobro)
        sol = sol.refresh()
        respond sol
    }

    def cobranzaContado(SolsFechaSucursalCommand command){
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = command.sucursal.id
        def pdf = reportService.run('FacturasCobrada', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaContado.pdf")
    }

    def cobranzaCod(SolsFechaSucursalCommand command) {
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = command.sucursal.id
        repParams['SALDOAFAVOR']=0.0
        def pdf = this.reportService.run('CobranzaCamioneta', repParams)
        def fileName = "CobranzaCOD.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }

    def disponibles(SolsFechaSucursalCommand command) {
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = command.sucursal.id
        def pdf = this.reportService.run('DisponiblesSucursal', repParams)
        def fileName = "DisponiblesSucursal.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }
    def ventasDiarias(SolsFechaSucursalCommand command) {
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = command.sucursal.id
        repParams['ORIGEN'] = params.origen
        def pdf = this.reportService.run('ventas_diarias', repParams)
        def fileName = "VentasDiarias.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }


}

@ToString(includeNames=true,includePackage=false)
class SolicitudFilter {
    Date fechaDeposito
    Date fechaCobranza

    static constraints = {
        fechaDeposito nullable: true
        fechaCobranza nullable: true
    }

}

class SolsFechaSucursalCommand {
    Date fecha
    Sucursal sucursal

    String toString() {
        return " ${sucursal.nombre} ${fecha.format('dd/MM/yyyy')}"
    }
}

class PosibleDuplicadoCommand {
    String banco
    String cuenta
    BigDecimal total
    Date fechaDeposito
    
}
