package sx.inventario


import grails.rest.RestfulController
import groovy.transform.ToString

import sx.core.Proveedor
import sx.core.Sucursal
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario
import sx.logistica.Chofer
import sx.reports.ReportService


@Secured("ROLE_INVENTARIO_USER")
class SolicitudDeTrasladoController extends  RestfulController{

    static responseFormats = ['json']

    SolicitudDeTrasladoService solicitudDeTrasladoService

    ReportService reportService

    public SolicitudDeTrasladoController() {
        super(SolicitudDeTraslado)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('Buscando solicitudes.... {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = SolicitudDeTraslado.where {}
        if(params.sucursal){
            query = query.where {sucursalSolicita.id ==  params.sucursal}
        }
        if( params.sucursalAtiende) {
            query = query.where {sucursalAtiende.id ==  params.sucursalAtiende}
        }
        if (params.porAtender) {
            query = query.where {atender == null}
        }
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {documento ==  documento}
        }
        return query.list(params)
    }




    protected SolicitudDeTraslado saveResource(SolicitudDeTraslado resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursalSolicita.clave
            resource.documento = Folio.nextFolio('SOLS',serie)
            resource.createUser = username
            resource.partidas.each { SolicitudDeTrasladoDet it ->
                it.comentario = resource.comentario
                it.recibido = it.solicitado

            }
        }
        resource.updateUser = username
        resource.save flush: true
        // return super.saveResource(resource)
    }

    protected Object updateResource(SolicitudDeTraslado resource) {
        if(resource.atender) {
            return solicitudDeTrasladoService.atender(resource)
        }
        return saveResource(resource)
    }

    def atender(SolicitudDeTraslado sol) {
        log.debug('Atendiendo sol {}', params)
        if(sol == null) {
            notFound()
            return
        }
        def chofer = Chofer.get(params.chofer_id)
        def comentario = params.comentario
        solicitudDeTrasladoService.atender(sol,chofer,comentario)
        respond sol

    }

    public buscarSolicitudPendiente(SolSearchCommand command){
        command.validate()
        if (command.hasErrors()) {
            respond command.errors // STATUS CODE 422
            return
        }
        def q = SolicitudDeTraslado.where{sucursalAtiende == command.sucursal && documento == command.documento }

        SolicitudDeTraslado res = q.find()
        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward action: 'show', id: res.id
    }
    def print() {
        // log.debug('Imprimiendo SolicitudDeTraslado.jrxml: ID:{}', params.ID)
        def pdf =  reportService.run('SolicitudDeTraslado.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'SolicitudDeTraslado.pdf')
    }

}


class SolSearchCommand {
    Sucursal sucursal
    Long documento

    String toString() {
        return "Criteria: ${sucursal.nombre} Folio: ${documento}";
    }

}

    
