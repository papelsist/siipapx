package sx.tesoreria

import com.luxsoft.utils.Periodo
import grails.rest.*
import grails.converters.*
import sx.core.Sucursal
import sx.cxc.Cobro
import sx.cxc.CobroTarjeta
import sx.reports.ReportService

class CorteDeTarjetaController extends RestfulController {

    static responseFormats = ['json']

    CorteDeTarjetaService corteDeTarjetaService

    ReportService reportService

    CorteDeTarjetaController() {
        super(CorteDeTarjeta)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('List: {}', params)
        params.max = 30
        params.sort = 'folio'
        params.order = 'asc'
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        def list = CorteDeTarjeta.findAll(
                "from CorteDeTarjeta c where date(c.corte) between ? and ? ",
                [periodo.fechaInicial, periodo.fechaFinal], [sort:'folio', order: 'asc', max: 50]
        )
        respond list
    }

    def pendientes() {
        log.debug('Pendientes: {}', params)
        Periodo periodo = new Periodo()
        bindData(periodo, params)
        log.debug(' Periodo: {}', periodo)
        List<CobroTarjeta> cobros = CobroTarjeta.where{
            corte == null && cobro.fecha == periodo.fechaInicial
        }.list()
        Map<String, List<CobroTarjeta>> grupos = cobros.groupBy {it.cobro.sucursal}
        //def list = grupos.collect { [sucursal: it.key, cobros: it.value]}
        List<CobrosPorSucursal> list = grupos.collect { it ->
            new CobrosPorSucursal(sucursal: it.key, cobros: it.value, fecha: periodo.fechaInicial)
        }
        // respond list
        respond(cobros: list)
    }

    def generarCortes() {
        CobrosPorSucursal cobrosPorSucursal = new CobrosPorSucursal();
        bindData cobrosPorSucursal, getObjectToBind()
        corteDeTarjetaService.generar(cobrosPorSucursal.fecha, cobrosPorSucursal.sucursal, cobrosPorSucursal.cobros)
        log.debug('Cobros por sucursal: {}', cobrosPorSucursal)
        respond status: 200
    }

    def ajustarCobro() {
        CobroTarjeta cobroTarjeta = new CobroTarjeta()
        bindData(cobroTarjeta, getObjectToBind())
        Cobro cobro = cobroTarjeta.cobro

        if(cobroTarjeta.debitoCredito) {
            cobro.tarjeta.debitoCredito = true
            cobro.tarjeta.visaMaster = true
            cobro.formaDePago = 'TARJETA_DEBITO'
        }

        if(cobroTarjeta.visaMaster && !cobroTarjeta.debitoCredito) {
            cobro.tarjeta.debitoCredito = false
            cobro.tarjeta.visaMaster = true
            cobro.formaDePago = 'TARJETA_CREDITO'
        }

        if(!cobroTarjeta.visaMaster){
            cobro.tarjeta.visaMaster = false
            cobro.tarjeta.debitoCredito = false
            cobro.formaDePago = 'TARJETA_CREDITO'
        }

        corteDeTarjetaService.actualizarComisiones(cobro.tarjeta)
        cobro.save flush:true
        respond cobro
    }

    def aplicar(CorteDeTarjeta corte){
        corte = corteDeTarjetaService.aplicar(corte)
        respond corte
    }
    def cancelarAplicacion(CorteDeTarjeta corte){
        if(corte.getEstatus() == 'APLICADO') {
            corte = corteDeTarjetaService.cancelarAplicacion(corte)
        }
        respond corte
    }

    @Override
    protected void deleteResource(Object resource) {
        corteDeTarjetaService.eliminarCorte(resource);
    }

    def reporteDeComisionesTarjeta(CortePorSucursal command){
        log.debug('Re: {}', command.fecha)
        // log.debug('Fecha: ', params.getDate('fecha'))
        def repParams = [FECHA_CORTE: command.fecha, SUCURSAL: command.sucursal.id]
        def pdf  = reportService.run('ComisionTarjetas.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ComisionTarjetas.pdf')
    }
}

class CobrosPorSucursal {

    Date fecha
    Sucursal sucursal
    List<CobroTarjeta> cobros

    String toString() {
        "${sucursal} : Cobros: ${cobros.size()} Fecha: ${fecha?.format('dd/MM/yyyy')}"
    }
}
class CortePorSucursal {
    Date fecha
    Sucursal sucursal
}
