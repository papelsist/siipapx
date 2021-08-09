package sx.inventario

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import sx.core.Sucursal
import sx.core.Existencia

@Secured("ROLE_INVENTARIO_USER")
class ConteoController extends RestfulController {

    static responseFormats = ['json']

    ConteoController() {
        super(Conteo)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 500
        def query = Conteo.where {}
        if(params.sucursal) {
            query = query.where { sucursal.id == params.sucursal}
        }
        return query.list(params).sort {it.sector.sectorFolio}
    }

    // @Override
    protected Conteo saveResource(Conteo resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Conteo updateResource(Conteo resource) {
        def username = getPrincipal().username
        resource.updateUser = username
        return super.updateResource(resource)
    }

    

    @Transactional
    def generarConteo() {
        def username = getPrincipal().username
        def today = new Date()
        def result = [:]
        def sectores = Sector.list([sort:'sectorFolio', order:'asc']);
        def conteos = [];
        sectores.each { sector ->
            Conteo conteo = Conteo.where{ sector == sector && fecha == today}.find()
             // println "Encontro conteo para sector ${sector.sectorFolio} y fecha ${today}"
            if( !conteo ){
                println "No encontro conteo para sector ${sector.sectorFolio} y fecha ${today}"
                conteo = new Conteo([
                sucursal: sector.sucursal,
                documento: sector.sectorFolio,
                fecha: new Date(),
                sector: sector,
                createUser: username
                ])
                sector.partidas.each { det ->
                    conteo.addToPartidas(new ConteoDet([producto: det.producto]))
                }
                conteo.updateUser = username
                conteo.save failOnError: true, flush:true
                conteos << conteo
            }   
            
        }
        result.message = 'Conteos generados exitosamente'
        result.conteos = conteos
        // println 'Res: '+ result
        respond(result, status: 200)
    }

    @Transactional
    def generarExistencias(Sucursal sucursal) {
        // println 'Params: ' + params
        assert sucursal.id, 'Debe indicar la sucursal para genera existencias conteo'

        def hoy = new Date()
        def result = [:]

        def found = ExistenciaConteo.where { fecha == hoy && sucursal == sucursal}.find()
        
        if(found) {
            result.message = 'Existencias ya generadas'
            respond(result, status: 200)
            return 
        }

        
        def ejercicio = hoy[Calendar.YEAR]
        def mes = hoy[Calendar.MONTH] + 1
        def existencias = Existencia.where {sucursal == sucursal && anio == ejercicio && mes == mes }
        existencias = existencias.where {producto.activo == true && producto.inventariable == true}.list()
        
        existencias.each {
            def ex = new ExistenciaConteo()
            ex.existencia = it
            ex.sucursal = it.sucursal
            ex.producto = it.producto
            ex.fecha = hoy
            ex.cantidad = it.cantidad
            ex.save failOnError: true , flush:true
            //println 'Existencia conteo generada: ' + ex.producto.clave
        }
        result.message = "${existencias.size()} exitencias generadas exitosamente"
        result.existencias = existencias.size()
        respond(result, status: 200)
    }

    @Transactional
    def limpiarExistencias(Sucursal sucursal){
        def hoy = new Date()
        ExistenciaConteo.where { fecha == hoy && sucursal == sucursal && fijado == null}.deleteAll()
        Map result = [:]
        result.message = "Existenicas para conteo eliminadas exitosamente "
        respond(result, status: 200)
    }
    

}


