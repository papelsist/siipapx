package sx.compras

import grails.rest.RestfulController
import groovy.transform.ToString
import sx.core.AppConfig
import sx.core.Existencia
import sx.core.Proveedor
import sx.core.Sucursal
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Folio
import sx.core.Inventario
import sx.reportes.PorFechaCommand
import sx.reports.ReportService


@Secured("ROLE_INVENTARIO_USER")
class RecepcionDeCompraController extends  RestfulController{

    static responseFormats = ['json']

    RecepcionDeCompraService recepcionDeCompraService

    ReportService reportService

    public RecepcionDeCompraController() {
        super(RecepcionDeCompra)
    }
    /*
    def index() {
        this.params.getBoolean('pendientes')
    }
    */

   @Override
    protected List listAllResources(Map params) {
       log.debug('List: {}', params)
       params.sort = 'lastUpdated'
       params.order = 'desc'
       def query = RecepcionDeCompra.where {sucursal.id ==  params.sucursal}
       if(params.getBoolean('pendientes')) {
            query = query.where {fechaInventario == null}
       }
       if(params.term) {
           if(params.term.isInteger()) {
               query = query.where{documento == params.term.toInteger() }
           } else {
               def search = '%' + params.term + '%'
               query = query.where { proveedor.nombre =~ search || remision =~ search}
           }
       }
       return query.list(params)
    }

    // @Override
    protected RecepcionDeCompra saveResource(RecepcionDeCompra resource) {
        def username = getPrincipal().username
        recepcionDeCompraService.save(resource, username)
    }

    protected RecepcionDeCompra updateResource(RecepcionDeCompra resource) {
        def username = getPrincipal().username
        resource.updateUser = username
        if(params.inventariar){
            recepcionDeCompraService.afectarInventario(resource)
        } else {
            resource.save flush: true
        }
        return resource
    }

    public buscarCompra(CompraParaComSearchCommand command){
       
        command.validate()
        if (command.hasErrors()) {
            respond command.errors, view:'create' // STATUS CODE 422
            return
        }
        def q = Compra.where{ (sucursal == command.sucursal || sucursal.nombre == 'OFICINAS') && folio == command.folio }
        q = q.where { cerrada != null && pendiente == true}
        Compra res = q.find()

        if (res == null) {
            notFound()
            return
        }
        // respond res, status: 200
        forward controller: 'compra', action: 'show', id: res.id
    }

    def recibir(RecepcionCompraCommand command) {
        if (command.compra == null) {
            notFound()
            return
        }
        log.debug('Generando recepcion autormatica de compra: {}', command.compra.folio)
        println "***  "+command.properties+"   ***"
 
        RecepcionDeCompra com = recepcionDeCompraService.recibir(command.compra, getPrincipal().username,command.remision,command.fechaR,command.comentario)
        respond com
    }

    def print(RecepcionDeCompra com) {
        params.ENTRADA = com.id
        params.SUCURSAL = com.sucursal.id
        def pdf =  reportService.run('EntradaPorCompra.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }

    def recepcionDeMercancia(PorFechaCommand command) {
        params.FECHA_ENT = command.fecha
        params.SUCURSAL = AppConfig.first().sucursal.id
        def pdf = this.reportService.run('RecepDeMercancia', params)
        def fileName = "RecepDeMercancia.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }
}

@ToString(includeNames=true,includePackage=false)
class RecepcionesFiltro {
    Date fechaInicial
    Date fechaFinal
    Sucursal sucursal
    Proveedor proveedor
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        sucursal nullable:true
        proveedor nullable: true
        registros size:(1..1000)

    }

    /*String toString(){
        return "$fechaInicial al $fechaFinal ${proveedor?.nombre}"
    }*/
}

class CompraParaComSearchCommand {

    Sucursal sucursal
    Long folio

    String toString(){
        return "$sucursal $folio"
    }

    static constraints = {
        
    }
}

class RecepcionCompraCommand{
    Compra compra
    String remision
    Date fechaR
    String comentario
     static constraints = {
        
    }
}
