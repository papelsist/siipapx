package sx.inventario


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.ExistenciaService
import sx.core.Folio
import sx.core.Inventario
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class TransformacionController extends RestfulController {

    static responseFormats = ['json']

    ReportService reportService

    ExistenciaService existenciaService

    TransformacionController() {
        super(Transformacion)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando transformaciones: {}', params)
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Transformacion.where { sucursal == AppConfig.first().sucursal }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { documento == params.term.toInteger() }
            } else {
                query = query.where { comentario =~ search}
            }
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
                existenciaService.afectarExistenciaEnAlta(inventario)
            }
            resource.fechaInventario = new Date()

        }
        resource.save flush:true
        return resource
        // return super.updateResource(resource)
    }

    def inventariar(Transformacion trs){

    }

    def print() {
        println 'Generando impresion para trs: '+ params
        def pdf = this.reportService.run('Transformacion', params)
        def fileName = "Transformacion.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
        
    }

    
}
