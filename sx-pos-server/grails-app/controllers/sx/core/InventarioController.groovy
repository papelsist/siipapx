package sx.core

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class InventarioController extends RestfulController {

    static responseFormats = ['json', 'xml']

    ReportService reportService;

    InventarioController() {
        super(Inventario)
    }

    protected List listAllResources(Map params) {
        log.debug('Localizando movimientos de inventario {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 200
        def query = Inventario.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { producto.clave =~ search || producto.descripcion =~ search}
        }
        def list = query.list(params)
        return list
    }

    def kardex(KardexCommand command){
        log.debug('Kardex: {}', command)
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def inicio= new Date(params.fechaIni)
        def fin= new Date(params.fechaFin)
        def inventarios= Inventario.where{producto==producto && fecha>= inicio && fecha<= fin}.list()
        respond inventarios:inventarios, inventarioCount:100
    }

    def printKardex(KardexCommand command) {
        log.debug('Imprimiendo kardex de: {}', command);
        log.debug('Kardex params: {}', params);
        params['FECHA_INI'] = command.fechaInicial
        params['FECHA_FIN'] = command.fechaFinal
        params['PRODUCTO'] = command.producto.id
        params['SUCURSAL'] = command.sucursal.id
        def pdf =  reportService.run('KardexSuc.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }
}

class KardexCommand {

    Producto producto
    Sucursal sucursal
    Date fechaInicial
    Date fechaFinal

    String toString() {
        return "${sucursal?.nombre} ${producto?.clave}  del ${fechaInicial?.format('dd/MM/yyyy')} al ${fechaFinal?.format('dd/MM/yyyy')}"
    }
}
