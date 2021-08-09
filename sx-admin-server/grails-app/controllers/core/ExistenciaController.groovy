package sx.core


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured


// @Secured("ROLE_INVENTARIO_USER")
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ExistenciaController extends RestfulController {

    static responseFormats = ['json']

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


    @Override
    protected List listAllResources(Map params) {
        addPeriodo(params)
        params.max = params.max ?: 20
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

    protected addPeriodo(Map params){
        Date today = new Date()
        params.year = params.year ?: today[Calendar.YEAR]
        params.mes  = params.mes ?: today[Calendar.MONTH] + 1
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
