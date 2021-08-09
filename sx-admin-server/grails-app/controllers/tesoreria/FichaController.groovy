package sx.tesoreria

import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus
import sx.core.AppConfig
import sx.core.Sucursal
import sx.cxc.CobroCheque
import sx.cxc.CuentaPorCobrar
import sx.reports.ReportService

class FichaController extends RestfulController {
    
    static responseFormats = ['json']

    FichaService fichaService

    ReportService reportService
    
    FichaController() {
        super(Ficha)
    }

    @Override
    protected List listAllResources(Map params) {
        //log.debug('Fichas list: {}', params)
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        String cartera = params.cartera ?: 'CREDITO'
        // log.debug('Buscando fichas para: {}', cartera)
        PorFechaCommand command = new PorFechaCommand()
        bindData(command, params)
        def query = Ficha.where {fecha == command.fecha}
        if(cartera == 'CREDITO'){
            query = query.where {origen == 'CRE'}
        } else {
            query = query.where {origen != 'CRE'}
        }
        if(params.sucursal) {
            String sucursal = params.sucursal
            def search = '%' + sucursal.toUpperCase() + '%'
            query = query.where { sucursal.nombre =~ search }
        }
        return query.list(params)
    }

    @Transactional
    def generar(FichasBuildCommand command) {
        log.debug('Generando fichas: {}', command)
        def res = fichaService.generar(command)
        // log.debug('Res: {}', res);
        respond status: HttpStatus.CREATED

    }

    def cheques() {
        // log.debug('Buscando cheques de ficha: {}', params)
        def fichaId = params.fichaId
        def list = CobroCheque.where {ficha.id == fichaId}.list()
        respond list
    }

    def ingreso() {
        Ficha ficha = Ficha.get(params.fichaId)
        log.debug('Registrando ingreso {}', ficha)
        ficha = fichaService.registrarIngreso(ficha)
        respond ficha
    }

    def reporteDeRelacionDeFichas(RelacionDeFichasCommand command){
        log.debug('Rep: params {}', params)
        log.debug('Rep command: {}', command)
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = command.origen
        repParams.SUCURSAL = command.sucursal.id
        def pdf =  reportService.run('RelacionDeFichas.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'RelacionDeFichas.pdf')
    }
}

class PorFechaCommand {

    Date fecha

    String toString() {
        return fecha.format('dd/MM/yyyy')
    }
}

class FichasBuildCommand {
    Date fecha
    String formaDePago
    String tipo
    CuentaDeBanco cuenta

    String toString() {
        return "${formaDePago} ${tipo} ${fecha.format('dd/MM/yyyy')} ${cuenta.descripcion}"
    }
}

class RelacionDeFichasCommand {
    Date fecha
    String origen
    Sucursal sucursal

}

