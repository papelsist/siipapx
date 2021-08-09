package sx.inventario

import com.luxsoft.utils.MonedaUtils
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Inventario
import sx.core.Sucursal
import sx.core.Venta
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class DevolucionDeVentaController extends RestfulController {

    static responseFormats = ['json']

    ReportService reportService

    DevolucionDeVentaService devolucionDeVentaService

    DevolucionDeVentaController() {
        super(DevolucionDeVenta)
    }

    @Override
    protected List listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 50
        def query = DevolucionDeVenta.where {}
        if(params.documento) {
            def documento = params.int('documento')

            query = query.where {documento ==  documento}
        }
        if(params.sucursal) {
            query = query.where { sucursal.id == params.sucursal}
        }
        return query.list(params)
    }

    // @Override
    protected DevolucionDeVenta saveResource(DevolucionDeVenta resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            resource.createUser = username
        }
        resource.updateUser = username
        return this.devolucionDeVentaService.save(resource)
    }

    protected actualizarRmd(DevolucionDeVenta rmd){
        def importeNeto = 0.0
        rmd.partidas.each {
            def cantidad = it.cantidad
            def factor = it.producto.unidad == 'MIL' ? 1000 : 1
            def precio = it.ventaDet.precio
            def subtotal = (cantidad/factor) * precio
            def descuento = it.ventaDet.descuento/100
            def descuentoImporte = subtotal * descuento
            def importe = MonedaUtils.round(subtotal - descuentoImporte)
            importeNeto = importeNeto + importe
            println "Importe en ventaDet ${it.ventaDet.subtotal} En rmd: ${importe}"
        }
    }


    public buscarVenta(VentaSearchCommand command){
       
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = Venta.where{sucursal == command.sucursal && cuentaPorCobrar.documento == command.documento && cuentaPorCobrar.tipo == command.tipo}
        if(command.fecha){
            q = q.where { fecha == command.fecha}
        }
        Venta res = q.find()
        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'venta', action: 'show', id: res.id
    }

    protected DevolucionDeVenta updateResource(DevolucionDeVenta resource) {
        if(params.inventariar){
            resource.partidas.each { det ->
                Inventario inventario = new Inventario()
                inventario.sucursal = resource.sucursal
                inventario.documento = resource.documento
                inventario.cantidad = det.cantidad
                inventario.comentario = det.comentario
                inventario.fecha = resource.fecha
                inventario.producto = det.producto
                inventario.tipo = 'RMD'
                det.inventario = inventario
            }
            resource.fechaInventario = new Date()
        }
        return super.updateResource(resource)
    }

    def print() {
        // log.debug('Imprimiendo SolicitudDeTraslado.jrxml: ID:{}', params.ID)
        Sucursal sucursal = AppConfig.first().sucursal
        params.SUCURSAL = sucursal.id
        def pdf =  reportService.run('Devoluciones.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'DevolucionDeVenta.pdf')
    }

}

class VentaSearchCommand {

    Sucursal sucursal
    String tipo
    Long documento
    Date fecha

    String toString(){
        return "$sucursal $tipo $documento - ${fecha?.format('dd/MM/yyyy')}"
    }

    static constraints = {
        fecha nullable:true
        tipo inList: ['CRE','CON','COD']
    }
}
