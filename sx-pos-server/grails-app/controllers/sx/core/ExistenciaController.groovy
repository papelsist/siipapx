package sx.core


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.reports.ReportService


// @Secured("ROLE_INVENTARIO_USER")
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ExistenciaController extends RestfulController {

    static responseFormats = ['json']

    ExistenciaService existenciaService

    ReportService reportService

    ExistenciaController() {
        super(Existencia)
    }

    def existenciasPorSucursal(ExistenciaFilter filter) {
        params.max = params.max ?: 20
        addPeriodo(params)
        def query = Existencia.where { }

        if(filter.sucursal) {
            query = Existencia.where { sucursal == filter.sucursal && anio == params.year && mes == params.mes}
        }

        if(filter.ejercicio) {
            query = query.where { anio == filter.ejercicio}
        }
        if(filter.mes ) {
            query = query.where { mes == filter.mes}
        }

        if(filtro.producto) {
            def search = '%' + params.prducto + '%'
            query = query.where { producto.clave =~ search || producto.descripcion =~ search}
        }
    }

    def existenciaRemota(Sucursal sucursal){
        def res = Existencia.where{ sucursal}
    }

    def acutlizarRecorte(RecorteCommand command){
        log.debug('Actualizando recorte: {} ', command);
        Existencia exis = command.existencia;

    }

    @Override
    protected Object updateResource(Object resource) {
        log.debug('Actualizando exis recorte: {} Recorte F: {} Rec com: {}', resource.recorte, resource.recorteFecha, resource.recorteComentario);
        def res = resource.save failOnError: true, flush:true
        log.debug('Actualizando existencia: {}', res)
        log.debug('Recorte: {} Recorte F: {} Rec com: {}', res.recorte, res.recorteFecha, res.recorteComentario);
        return res

        // return super.updateResource(resource)
    }

    @Override
    protected List listAllResources(Map params) {
        addPeriodo(params)
        log.debug('Exis: {}', params)
        params.max = params.max ?: 40
        params.sort = 'lastUpdated'
        params.order = 'desc'
        def query = Existencia.where {anio == params.year && mes == params.mes}
        if( params.ejercicio) {
            query = query.where { anio == params.int('ejercicio')}
        }
        if( params.int('mes')) {
            query = query.where { mes == params.int('mes')}
        }
        if (params.activos) {
            query = query.where { producto.activo == true}
        }
        if(params.conexistencia) {
            query = query.where { cantidad > 0}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            query = query.where { producto.clave =~ search || producto.descripcion =~ search}
        }

        if(params.sucursal) {
            query = query.where { sucursal.id == params.sucursal }
        }
        return query.list(params)
    }

    def buscarExistencias(BuscarExistenciasCommand command) {
        def existencias = Existencia.where { 
            producto == command.producto && 
            anio == command.year && 
            mes == command.month}
            .list()
        respond existencias
    }

    def recalcular(){
        Date hoy = new Date()
        def ejercicio = hoy[Calendar.YEAR]
        def mes = hoy[Calendar.MONTH] + 1
        Producto producto = params.producto ? Producto.get(params.producto) : null
        log.debug('Recalculando existencias: Ejercicio: {}  Mes: {} Para: {}', ejercicio, mes, producto? producto.clave: 'Todos los productos')
        if (producto)
            existenciaService.recalcular(producto, ejercicio, mes)
        else
            existenciaService.recalcular(ejercicio, mes)
        respond [:]
    }

    protected addPeriodo(Map params){
        Date today = new Date()
        params.year = params.year ?: today[Calendar.YEAR]
        params.mes  = params.mes ?: today[Calendar.MONTH] + 1
    }

    def reporteDeDiscrepancias() {

        def fecha=params.fecha
        def repParams = [:]
        repParams['SUCURSAL'] = AppConfig.first().sucursal.id.toString()
        repParams['FECHA'] = fecha

        println "**************************"+repParams
        println "-----"+fecha.format('yyyy/MM/dd')

        def pdf =  reportService.run('DiscrepanciasDeInv', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'DiscrepanciasDeInv.pdf')
    }

    def recortePorDetalle() {
        params.SUCURSAL = AppConfig.first().sucursal.id.toString()
        def pdf =  reportService.run('RecorteXDetalle', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'RecortePorDetalle.pdf')
    }

}

class ExistenciaFilter {
    Sucursal sucursal
    Integer ejercicio
    Integer mes
    String producto = '%'

    ExistenciaFilter() {
        Date today= new Date();
        //Calendar.getAt(Calendar.YEAR)
        //this.mes = today[Calendar.MONTH]
    }

    static constraints = {
        sucursal nullable: true
        mes nullable: true
        ejercicio nullable: true
    }

    String toString(){
        "$sucursal $ejercicio $mes"
    }
}

class BuscarExistenciasCommand {
    
    
    Producto producto
    Integer year
    Integer month


    String toString(){
        return "$producto $year $month"
    }
}

class RecorteCommand {
    Existencia existencia
    BigDecimal cantidad
    String comentario
}
