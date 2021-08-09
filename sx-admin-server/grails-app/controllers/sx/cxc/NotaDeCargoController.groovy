package sx.cxc

import com.luxsoft.cfdix.v33.NotaDeCargoPdfGenerator
import com.luxsoft.utils.MonedaUtils
import com.luxsoft.utils.Periodo
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.web.http.HttpHeaders
import sx.reports.ReportService

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCargoController extends RestfulController {
    
    static responseFormats = ['json']

    NotaDeCargoService notaDeCargoService

    ReportService reportService

    NotaDeCargoController() {
        super(NotaDeCargo)
    }

    @Override
    Object save() {
        NotaDeCargo nota = createResource()
        nota.validate(['cliente','total'])
        if (nota.hasErrors()) {
            // transactionStatus.setRollbackOnly()
            respond nota.errors, view:'create' // STATUS CODE 422
            return
        }
        nota = notaDeCargoService.save(nota);
        respond nota, [status: CREATED, view:'show']
    }

    @Override
    protected Object updateResource(Object resource) {
        notaDeCargoService.update(resource)
    }

    @Override
    protected Object createResource() {
        NotaDeCargo instance = new NotaDeCargo()
        bindData instance, getObjectToBind()
        instance.serie = 'CAR'
        instance.impuesto = 0.0
        // instance.total = 0.0
        return instance
    }

    @Override
    protected void deleteResource(Object resource) {
        notaDeCargoService.delete(resource)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List {}', params)
        def query = NotaDeCargo.where {}
        params.max = 30
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.term) {
            log.debug('Term: {}', params.term)
            if(params.term.isInteger()) {
                query = query.where{folio == params.term.toInteger()}
            } else {
                def search = '%' + params.term + '%'
                query = query.where { cliente.nombre =~ search}
            }
        }
        return query.list(params)
    }


    def print(NotaDeCargo nota) {
        assert nota.cfdi, 'Nota sin timbrar: ' + nota.id
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = NotaDeCargoPdfGenerator.getReportData(nota)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        def pdf  = reportService.run('PapelCFDI3Nota.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'NotaDeCredito.pdf')
    }


    def timbrar(NotaDeCargo nota) {
        assert !nota.cfdi, 'Nota ya timbrada'
        nota = notaDeCargoService.timbrar(nota)
        respond nota
    }

    def reporteDeNotasDeCargo() {
        // log.debug('Re: {}', params)
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        def repParams = [
            FECHA_INI: periodo.fechaInicial,
            FECHA_FIN: periodo.fechaFinal,
            ORIGEN: params.ORIGEN,
        ]
        def pdf  = reportService.run('NotasDeCargo.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ComisionTarjetas.pdf')
    }
}
