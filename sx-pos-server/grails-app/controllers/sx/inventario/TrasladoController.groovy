package sx.inventario

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.converters.*
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class TrasladoController extends RestfulController {

    static responseFormats = ['json']

    SolicitudDeTrasladoService solicitudDeTrasladoService

    TrasladoService trasladoService

    ReportService reportService

    TrasladoController() {
        super(Traslado)
    }

    @Override
    protected List listAllResources(Map params) {
        params.max = 100;
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Traslado.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.tipo) {
            query = query.where {tipo == params.tipo}
        }
        return query.list(params)
    }

    def salida(Traslado tps){
        if(tps == null) {
            notFound()
            return
        }
        assert tps.tipo == 'TPS', 'El registro de salida es para trasplados tipo TPS'
        def res = trasladoService.registrarSalida(tps)
        respond res

    }

    def print() {
        log.debug('Imprimiendo SalidaDeTraslado.jrxml: ID:{}', params.ID)
        params.TRASLADO_ID = params.ID
        def pdf =  reportService.run('SalidaDeTraslado.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Traslado.pdf')
    }
}
