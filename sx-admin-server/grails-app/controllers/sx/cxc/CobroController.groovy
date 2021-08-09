package sx.cxc


import com.luxsoft.cfdix.v33.ReciboDePagoPdfGenerator
import com.luxsoft.utils.Periodo
import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.ToString
import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.AppConfig
import sx.core.Cliente
import sx.reports.ReportService
import sx.core.Sucursal


@Secured("hasRole('ROLE_CXC_USER')")
// @GrailsCompileStatic
class CobroController extends RestfulController<Cobro>{

    CobroService cobroService

    ReportService reportService

    ChequeDevueltoService chequeDevueltoService

    static responseFormats = ['json']

    CobroController() {
        super(Cobro)
    }

    @Override
    protected List<Cobro> listAllResources(Map params) {
        log.debug('List {}', params)
        def query = Cobro.where {}
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        if(params.cartera) {
            String tipoCartera = params.cartera
            if(tipoCartera == 'CON') {
                tipoCartera = 'COD'
            }
            query = query.where { tipo == tipoCartera}
        }
        if(params.sucursal) {
            query = query.where {sucursal == Sucursal.get(params.sucursal)}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search || formaDePago =~ search }
        }
        return query.list(params)
    }

    def search(CobroSearchCommand command) {
        log.debug('Search: {}', command)
        params.max = command.registros
        params.sort = params.sort ?:'fecha'
        params.order = params.order ?:'asc'

        def query = Cobro.where { formaDePago != 'PAGO_DIF' }
        /*
        if(params.cartera) {
            String tipoCartera = params.cartera
            if(tipoCartera == 'CON') {
                tipoCartera = 'COD'
            }
            query = query.where { tipo == tipoCartera}
        }
        */

        if(params.cartera){
            def cart = params.cartera
            log.info('Cartera: {}', cart)
            switch (cart) {
                case 'CON':
                    query = query.where{ tipo == 'COD'}
                    break
                default:
                    query = query.where{ tipo == cart}
            }
        }

        if(command.periodo) {
            query = query.where { fecha >= command.periodo.fechaInicial}
            query = query.where { fecha <= command.periodo.fechaFinal}
        }
        if(command.pendientes) {

            query = query.where { saldo > 0.0}
        }
        if(command.cliente) {
            query = query.where {cliente == command.cliente}
        }
        respond query.list(params)
    }

    def disponibles() {
        log.debug('Disponibles {}', params)
        String hql = 'from Cobro c where c.importe - c.aplicado -c.diferencia > 0  and tipo like ? order by fecha asc'
        params.max = 300
        String cartera = params.cartera ?: '%'

        if(params.term) {
            def search = '%' + params.term + '%'
            hql = 'from Cobro c where c.importe - c.aplicado - c.diferencia > 0  and tipo like ? ' +
                    ' and c.cliente.nombre like ? ' +
                    ' order by fecha asc'
            respond Cobro.findAll(hql, [cartera, search], params)
        } else {
            respond Cobro.findAll(hql, [cartera], params)
        }

    }



    protected Cobro createResource() {
        log.debug('Generando cobro: {}', params)
        Cobro cobro =  new Cobro()
        bindData cobro, getObjectToBind()
        cobro.sucursal = AppConfig.first().sucursal
        return cobro
    }

    def cobrosMonetarios(CobranzaPorFechaCommand command) {
        // log.debug('Cobros {}', params)
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        // def query = Cobro.where { sucursal == AppConfig.first().sucursal }
        def query = Cobro.where {  }
        if(command.getFecha()) {
            // log.debug('Cobros por fecha Ini: {}, {}', command.fecha, command.fechaFin)
            if(command.fechaFin == null )
                command.fechaFin = command.fecha
            query = query.where {fecha >= command.fecha && fecha <= command.fechaFin}
        }

        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if(params.formaDePago) {
            def search = '%' + params.formaDePago + '%'
            query = query.where { formaDePago =~ search}
        }
        if(params.importe) {
            BigDecimal importe = params.importe as BigDecimal
            query = query.where {importe == importe}
         }

        query = query.where{formaDePago == 'CHEQUE' || formaDePago == 'EFECTIVO' || formaDePago == 'TARJETA_DEBITO' || formaDePago == 'TARJETA_CREDITO'}
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search  || referencia =~search}
        }

        respond query.list(params)
    }




    protected Cobro saveResource(Cobro resource) {
        return cobroService.save(resource)
    }


    protected Cobro updateResource(Cobro resource) {
        if (resource.pendientesDeAplicar) {
            // log.debug('Facturas por aplicar : {}', resource.pendientesDeAplicar)
            return cobroService.registrarAplicacion(resource)
        }
        // return super.updateResource(resource)
        return cobroService.save(resource)
    }

    def saldar(Cobro cobro){
        log.debug('Saldando cobro: {}', cobro)
        cobro = cobroService.saldar(cobro)
        forward action: 'show', id: cobro.id
    }

    def registrarChequeDevuelto(Cobro cobro){
        assert cobro.cheque, "El cobro debe de ser tipo cheque y tener registro de CobroCheque. Cobro tipo: ${cobro.tipo}"
        Date fecha = params.getDate('fecha', 'dd/MM/yyyy')
        this.chequeDevueltoService.registrarChequeDevuelto(cobro.cheque, fecha)
        cobro.comentario = "CHEQUE DEVUELTO EL: ${fecha.format('dd/MM/yyyy')}"
        cobro.save flush: true
        respond cobro
    }

    def reporteDeCobranza(CobranzaPorFechaCommand command){
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.cartera
        def pdf =  reportService.run('CobranzaCxc.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CobranzaCxc.pdf')
    }

    def reporteDeRelacionDePagos(RelacionPagosCommand command){
        log.debug('Rep: {}', params)
        def repParams = [FECHA: command.fecha]
        repParams.ORIGEN = params.origen
        repParams.COBRADOR = command.cobrador == 0 ? '%': command.cobrador.toString()
        def pdf =  reportService.run('RelacionDePagos.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'RelacionDePagos.pdf')
    }

    def reporteDeRecibosPendientes(){
        log.debug('Rep: {}', params)
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        def repParams = [:]
        repParams['FECHA_INI'] = periodo.fechaInicial
        repParams['FECHA_FIN'] = periodo.fechaFinal
        def pdf =  reportService.run('CobrosPendientesDeCFDIdeCOD.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CFDIs pendientes.pdf')
    }

    def timbrar(Cobro cobro) {
        cobro = cobroService.timbrar(cobro)
        forward action: 'show', id: cobro.id
    }

    def timbradoBatch(TimbradoBatchCommand command) {
        List<Cobro> timbrados = []
        command.cobros.each {
            timbrados << cobroService.timbrar(it)
        }
        respond timbrados
    }

    def imprimirRecibo(Cobro cobro) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = ReciboDePagoPdfGenerator.getReportData(cobro)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        def pdf  = reportService.run('ReciboDePagoCFDI33.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ReciboDePagoCFDI33.pdf')
    }

    def aplicar(AplicarCobroCommand command) {
        log.debug('Aplicar cobro: {} a cuentas: {}', command.cobro.id, command.cuentas.collect{it.id}.join(','))
        Cobro cobro = cobroService.registrarAplicacion(command.cobro, command.cuentas)
        cobro.refresh()
        forward action: 'show', id: command.cobro.id
    }

    def eliminarAplicacion(AplicacionDeCobro aplicacionDeCobro) {
        if(aplicacionDeCobro == null) {
            notFound()
            return
        }
        Cobro cobro = cobroService.eliminarAplicacion(aplicacionDeCobro)
        cobro.refresh()
        forward action: 'show', id: cobro.id
    }


    def handleException(Exception e) {
        e.printStackTrace()
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }

    // def show(Cobro cobro) {
    //     log.info('Show2 cobro: {} {}', cobro.id, cobro.cliente.nombre)
    //     respond(cobro, view: 'get')
    // }
 }

class PorSucursalFechaRepCommand {
    Sucursal sucursal
    Date fecha
}

class CobranzaPorFechaCommand {
    Date fecha
    Date fechaFin

    static constraints = {
        fechaFin nullable: true
    }
}

class RelacionPagosCommand {
    Date fecha
    String origen
    Integer cobrador
}

class AplicarCobroCommand {
    Cobro cobro
    List<CuentaPorCobrar> cuentas
}

@ToString(includeNames=true,includePackage=false)
class CobroSearchCommand {
    String cartera
    boolean pendientes = true
    Integer registros = 10
    Periodo periodo
    Cliente cliente


    static constraints = {
        periodo nullable: true
        cliente nullable: true
    }

}

class TimbradoBatchCommand {
    List<Cobro> cobros
}


