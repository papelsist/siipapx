package sx.cxc

import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController

import sx.cxc.ChequeDevuelto
import sx.cxc.ChequeDevueltoService
import sx.reports.ReportService

// @GrailsCompileStatic
class ChequeDevueltoController extends RestfulController<ChequeDevuelto> {

    ChequeDevueltoService chequeDevueltoService

    ReportService reportService

    ChequeDevueltoController() {
        super(ChequeDevuelto);
    }

    @Override
    protected List<ChequeDevuelto> listAllResources(Map params) {
        params.max = 500
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = ChequeDevuelto.where {}
        respond query.list(params)
    }

    @Override
    protected ChequeDevuelto saveResource(ChequeDevuelto resource) {
        return this.chequeDevueltoService.save(resource)
    }

    @Override
    protected ChequeDevuelto updateResource(ChequeDevuelto resource) {
        return this.chequeDevueltoService.update(resource)
    }

    def reporteDeChequesDevueltos() {
        def repParams = [FECHA_INI: params.getDate('fecha', 'dd/MM/yyyy')]
        def pdf =  reportService.run('ChequesDevueltos.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ChequesDevueltos.pdf')
    }

    def estadoDeCuentaGeneralChe() {
        def fecha = params.getDate('fecha', 'dd/MM/yyyy');
        params.FECHA = fecha
        def pdf = reportService.run('EstadoDeCuentaGralChe.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'EstadoDeCuentaGralChe.pdf')

    }

    def estadoDeCuentaDetChe() {
        def fecha = params.getDate('fecha', 'dd/MM/yyyy');
        params.FECHA = fecha
        def pdf = reportService.run('EstadoDeCuentaDetChe.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'EstadoDeCuentaGralChe.pdf')

    }

    def saldosPorAbogado() {
        def fecha = params.getDate('fecha', 'dd/MM/yyyy');
        def dpo = DespachoDeCobranza.get(params.despacho)
        def repParams = [:]
        repParams.FECHA = fecha
        repParams.ABOGADO = dpo ? dpo.nombre : '%'
        println 'PARAMS: ' + repParams
        def pdf = reportService.run('SaldosPendientesPorAbogado.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'SaldosPendientesPorAbogado.pdf')
    }
}
