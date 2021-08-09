package sx.compras

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.core.ProveedorCompras
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("ROLE_COMPRAS_USER")
class CompraController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService;

    CompraController(){
        super(Compra)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'fecha'
        params.order = 'desc'
        params.max = 20
        log.info('List {}', params)

        def query = Compra.where {}

        Boolean pendientes = this.params.getBoolean('pendientes')

        if(pendientes) {
            params.max = 100
            query = query.where { pendiente == true}
        }
        if(params.boolean('transito')){
            query = query.where {cerrada != null && pendiente == true}
        }
        if( params.folio) {
            query = query.where {folio == params.int('folio') }
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { proveedor.nombre =~ search || comentario =~ search}
        }
        return query.list(params)
    }

    @Override
    protected Object createResource() {
        Compra compra = new Compra()
        bindData compra, getObjectToBind()
        compra.folio = 0
        compra.sucursal = AppConfig.first().sucursal
        compra.partidas.each { CompraDet det ->
            det.sucursal = compra.sucursal
            det.clave = det.producto.clave
            det.descripcion = det.producto.descripcion
            det.unidad = det.producto.unidad

        }
        compra.nombre = compra.proveedor.nombre
        compra.rfc = compra.proveedor.rfc
        compra.clave = compra.proveedor.clave
        return compra
    }


    protected Object updateResource(Compra resource) {
        // log.info('Actualizando compra: {} ', resource.folio)
        resource.partidas.each {
            if(it.sucursal == null)
                it.sucursal = resource.sucursal
            it.clave = it.producto.clave
            it.descripcion = it.producto.descripcion
            it.unidad = it.producto.unidad
        }
        resource.save flush: true
    }

    @Override
    protected Object saveResource(Object resource) {
        resource.folio = Folio.nextFolio('COMPRA','OFICINAS')
        // resource.createdBy = getPrincipal().username
        resource.save flush: true
    }

    def print( ) {
        //params.ID = params.id;
        def pdf =  reportService.run('OrdenDeCompraSuc.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'OrdenDeCompraSuc.pdf')
    }

    def cerrar(Compra compra) {
        if(compra.cerrada == null ){
            compra.cerrada = new Date()
            compra.save flush: true
            respond compra
            return
        }
        respond compra
    }

    def depurar(DepuracionCommand command) {
        // log.info ('Dep: {}', command)
        if(command.partidas) {
            Compra compra = command.compra
            command.partidas.each { String id ->
                CompraDet det = compra.partidas.find { it.id == id}
                det.depurado = det.getPorRecibir()
                det.depuracion = new Date()
                compra.ultimaDepuracion = det.depuracion
            }
            compra.actualizarStatus();
            compra.save failOnError: true, flush: true
            respond compra
            return
        } else {
            respond status: 200
        }

    }

}

class DepuracionCommand {
    Compra compra
    List<String> partidas

    String toString() {
        "Depurando compra ${compra.folio}. ${partidas?.size()} partidas a depurar"
    }
}

