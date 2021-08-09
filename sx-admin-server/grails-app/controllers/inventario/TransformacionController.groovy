package sx.inventario


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio
import sx.core.Inventario

@Secured("ROLE_INVENTARIO_USER")
class TransformacionController extends RestfulController {

    static responseFormats = ['json']

    def reporteService

    TransformacionController() {
        super(Transformacion)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando transformaciones: {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Transformacion.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}   
        }
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {documento >=  documento}
        }
        return query.list(params)
    }

    // @Override
    protected Transformacion saveResource(Transformacion resource) {
        def username = getPrincipal().username
        
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('TRANSFORMACION',serie)
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }


    protected Transformacion updateResource(Transformacion resource) {

        if(params.inventariar){
            resource.partidas.each { det ->
                Inventario inventario = new Inventario()
                inventario.sucursal = resource.sucursal
                inventario.documento = resource.documento
                inventario.cantidad = det.cantidad
                inventario.comentario = det.comentario
                inventario.fecha = resource.fecha
                inventario.producto = det.producto
                inventario.tipo = resource.tipo
                det.inventario = inventario
            }
            resource.fechaInventario = new Date()

        }

        return super.updateResource(resource)
    }

    def inventariar(Transformacion trs){

    }

    def print() {
        println 'Generando impresion para trs: '+ params
        def pdf = this.reporteService.run('Transformacion', params)
        def fileName = "Transformacion.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
        
    }

    
}
