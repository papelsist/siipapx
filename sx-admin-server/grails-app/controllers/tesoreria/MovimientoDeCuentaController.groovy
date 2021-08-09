package sx.tesoreria


import grails.rest.*
import grails.converters.*

class MovimientoDeCuentaController extends RestfulController {
    static responseFormats = ['json', 'xml']
    MovimientoDeCuentaController() {
        super(MovimientoDeCuenta)
    }

    // def index(RecepcionesFiltro filtro){
    //     log.info('Buscando movimientos con filtro: ' + filtro)
    //     params.max = filtro.registros ?:10
    //     def query  = RecepcionDeCompra.where {}
    //     if(filtro.fechaInicial){
    //         Date inicio = filtro.fechaInicial
    //         Date fin = filtro.fechaFinal ?: inicio
    //         query = query.where {fecha >= inicio && fecha <= fin}
    //     }
    //     if (filtro.sucursal) {
    //         query = query.where { sucursal == filtro.sucursal}
    //     }
    //     if(filtro.proveedor) {
    //         query = query.where {proveedor == filtro.proveedor}
    //     }
    //     respond query.list(params)
    // }

    
    protected List listAllResources(Map params) {
        log.info('Cargando movimientos con parametros: ' + params)
        def query = MovimientoDeCuenta.where {}
        if(params.formaDePago) {
        	query = query.where { formaDePago =~ params.formaDePago }
        }
        // def query = RecepcionDeCompra.where {}
        // params.max = params.registros ?:10
        // params.sort = params.sort ?:'folio'
        // params.order = params.order ?:'desc'

        // if(params.term){
        //     def search = '%' + params.term + '%'
        //     query = query.where { proveedor.nombre =~ search }
        // }

        return query.list(params)
        //return MovimientoDeCuenta.list(params)
    }
}

class MovimientosDeCuentaFiltro {
    
    Date fechaInicial
    Date fechaFinal
    // Sucursal sucursal
    // Proveedor proveedor
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        // sucursal nullable:true
        // proveedor nullable: true
        registros size:(1..100)

    }

    String toString(){
        return "$fechaInicial al $fechaFinal ${proveedor?.nombre}"
    }
}