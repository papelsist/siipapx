package sx.cxc

import com.luxsoft.utils.ImporteALetra
import com.luxsoft.utils.Periodo
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Cliente
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class CuentaPorCobrarController extends RestfulController<CuentaPorCobrar>{

    static responseFormats = ['json']

    ReportService reportService

    CuentaPorCobrarService cuentaPorCobrarService

    AntiguedadService antiguedadService

    CuentaPorCobrarController() {
        super(CuentaPorCobrar)
    }

    @Override
    protected List listAllResources(Map params) {

        def query = CuentaPorCobrar.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'


        if(params.documento){
          int documento = params.int('documento')
          query = query.where { documento >= documento }
        }
        if(params.cliente){
            query = query.where { cliente.id == params.cliente}
        }
        return query.list(params)
    }

    def search() {
        def query = CuentaPorCobrar.where { }
        params.sort = params.sort ?: 'lastUpdated'
        params.order = params.order ?:'desc'
        params.max = 50

        log.info("Search: {}", params)



        if(params.cartera){
            def cart = params.cartera
            switch (cart) {
                case 'CRE':
                    query = query.where{ tipo == 'CRE' && juridico == null}
                    break
                case 'CON':
                    query = query.where{ tipo == 'CON' || tipo == 'COD' && juridico == null}
                    break
                case 'JUR':
                    query = query.where{juridico != null}
                    break
                default:
                    query = query.where{ tipo == cart && juridico == null}
            }
        }



        String nombre = params.nombre
        if(nombre){
            def search = '%' + nombre + '%'
            query = query.where { cliente.nombre =~ search}
        }

        String sucursal = params.sucursal
        if(sucursal){
            def search = '%' + sucursal + '%'
            query = query.where { sucursal.nombre =~ search}
        }

        Integer documento = params.getInt('documento')
        if(documento)
            query = query.where { documento == documento }

        if(params.fechaInicial) {
            Date fechaInicial = params.getDate('fechaInicial', 'yyyy-MM-dd')
            Date fechaFinal = params.getDate('fechaFinal', 'yyyy-MM-dd') ?: fechaInicial
            query = query.where { fecha >= fechaInicial && fecha <= fechaFinal}

        }

        respond query.list(params)

    }

    def pendientes(Cliente cliente) {
        log.info('Pendientes {}', params)
        if (cliente == null) {
            notFound()
            return
        }
        // params.max = 100
        params.sort = params.sort ?:'fecha'
        params.order = params.order ?:'asc'
        def cartera = params.cartera ?: 'CRE'
        def rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  where c.cliente = ? and c.tipo = ? and c.total - c.pagos > 0 ",
                [cliente, cartera])
        if(cartera == 'JUR') {
            rows = CuentaPorCobrar.findAll(
                    "from CuentaPorCobrar c  where c.cliente = ?  and c.total - c.pagos > 0 ",
                    [cliente])
        }

        log.debug('Cuentas por cobrar para: {} : {}', cliente.nombre, rows.size())
        respond rows
    }

    def antiguedad(){
        // Antiguedad de saldos
        log.info('Generando antiguedad de saldos {}', params);
        def rows = antiguedadService.antiguedad()
        respond rows
    }

    def saldar(CuentaPorCobrar cxc) {
        log.debug('Saldando cuenta por cobrar: {}', cxc.folio)
        cuentaPorCobrarService.saldar(cxc)
        cxc.refresh()
        respond cxc
    }

    def printAntiguedad() {
        log.debug('Params: {}', params)
        Date fecha = params.getDate('fecha', 'dd/MM/yyyy')
        def repParams = [:]
        repParams.CORTE = fecha
        repParams.ORDER = params.int('sort')
        repParams.FORMA = params.order
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('AntiguedadSaldosGral.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }

    def reporteDeCobranzaCOD(CobranzaCodCommand command) {
        def repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy-MM-dd')
        repParams.SUCURSAL = command.sucursal.id
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('CarteraCOD_Emb.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CarteraCOD.pdf')
    }

    def antiguedadPorCliente(AntiguedadPorCteCommand command) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def repParams = [:]
        repParams.FECHA_CORTE = command.fecha
        repParams.CLIENTE = command.cliente.id
        def pdf = reportService.run('AntiguedadSaldosConCortePorCliente.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }

    def clientesSuspendidosCre() {
        def repParams = [:]
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('ClientesSuspendidosCredito.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ClientesSuspendidosCredito.pdf')
    }

    def facturasConNotaDevolucion(FacturasConNtaCommand command) {
        def repParams = [:]

        repParams.SUCURSAL = command.sucursal ? command.sucursal.id : '%'
        repParams.FECHA_INI = command.fechaIni
        repParams.FECHA_FIN = command.fechaFin
        if(command.origen == 'CRE') {
            repParams.ORIGEN = 'CRE'
        } else if(command.origen == 'CON') {
            repParams.ORIGEN = 'CO%'
        } else {
            repParams.ORIGEN = '%'
        }

        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('FacsCancPorNotaDev.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'FacsCancPorNotaDev.pdf')

    }

    def reporteExceptionesDescuentos(FacturasConNtaCommand command) {
        def repParams = [:]
        repParams.SUCURSAL = command.sucursal ? command.sucursal.id : '%'
        repParams.FECHA_INI = command.fechaIni
        repParams.FECHA_FIN = command.fechaFin
        repParams.ORIGEN = command.origen
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('ExcepcionesEnDescuento.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'FacsCancPorNotaDev.pdf')
    }


    def generarPagare() {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'

        CuentaPorCobrar cxc = CuentaPorCobrar.get(params.id)
        Map repParams = [ID: params.id]
        repParams.TELEFONOS = "Tels: " + cxc.cliente.telefonos.join(', ')
        repParams.IMPORTE_LETRA = ImporteALetra.aLetra(cxc.total)
        def pdf = reportService.run('Pagare.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pagare.pdf')
    }




}

class CobranzaCodCommand {
    Sucursal sucursal
    Date fecha

    static constraints = {
        sucursal nullable: true
    }
}

class AntiguedadPorCteCommand {
    Date fecha
    Cliente cliente
}

class FacturasConNtaCommand {
    Date fechaIni
    Date fechaFin
    Sucursal sucursal
    String origen

    static constraints = {
        sucursal nullable: true
    }
}
