package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.cxc.CobranzaPorFechaCommand
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar
import sx.cxc.NotaDeCredito
import sx.reports.ReportService

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ClienteController extends RestfulController<Cliente>{

    static responseFormats = ['json']

    ClienteService clienteService

    ReportService reportService

    ClienteController(){
        super(Cliente)
    }

    @Override
    protected Cliente updateResource(Cliente resource) {
        
        log.info('Actualizando cliente: {}', resource)
        return clienteService.updateCliente(resource)
    }

    @Override
    protected List<Cliente> listAllResources(Map params) {
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Cliente.where {}
        log.info('List: {}', params)
        if (params.cartera) {
            if(params.cartera.startsWith('CRE') ){
                query = query.where {credito != null && credito.lineaDeCredito != null}
            } else if (params.cartera.startsWith('CON') || params.cartera.startsWith('COD')) {
                // log.debug('BUSCANDO : {}', params)
                // query = query.where {credito == null }
            }
        }
        if(params.tipo) {
            def tipo = params.tipo
            if(tipo == 'CREDITO') {
                log.info('CREDITO+++: {}', tipo)
                query = query.where {credito != null && credito.lineaDeCredito != null}
            } else if(tipo == 'CONTADO'){
                query = query.where {credito == null}
            } 
        }
        if(params.term){
            def search = '%' + params.term + '%'
            // log.debug('Search: {}', search)
            query = query.where { nombre =~ search}
        }
        return query.list(params)
    }


    /**** Finders ****/
    def facturas(Cliente cliente){
        params.max = 100
        params.sort = 'fecha'
        params.order = 'desc'
        def query = CuentaPorCobrar.where {cliente == cliente}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { documento == params.term.toInteger() }
            }
        }
        List res = query.list(params)
        respond res

    }
    def cxc(Cliente cliente){
        def rows = CuentaPorCobrar
                .findAll("from CuentaPorCobrar c  where c.tipo = 'CRE' and c.cliente = ? and c.total - c.pagos > 0 ", [cliente])

        respond rows.sort {it.atraso}.reverse()
    }

    def notas(Cliente cliente){
        params.max = 20
        params.sort = 'fecha'
        params.order = 'desc'
        def query = NotaDeCredito.where {cliente == cliente}
        if(params.tipo) {
            def tipo = params.tipo
            query = query.where {tipo == tipo}
        }
        respond query.list(params)
    }

    def cobros(Cliente cliente){
        params.max = 100
        params.sort = 'fecha'
        params.order = 'desc'
        def rows = Cobro.where {cliente == cliente}.list(params)
        respond rows
    }

    def socios(Cliente cliente){
        params.sort = 'nombre'
        params.order = 'asc'
        def rows = Socio.where {cliente == cliente}.list(params)
        respond rows
    }

    def estadoDeCuenta(CobranzaPorFechaCommand command){
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.cartera
        repParams.CLIENTE = params.cliente
        def pdf =  reportService.run('EstadoDeCuentaCte.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CobranzaCxc.pdf')
    }



}
