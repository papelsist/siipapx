package sx.inventario

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import sx.core.Sucursal
import sx.core.Existencia
import sx.reports.ReportService



@Secured("ROLE_INVENTARIO_USER")
//@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ConteoController extends RestfulController {
    
    ReportService reportService

    ConteoService conteoService

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

        def result = [:]
        def found = Conteo.findAll("from Conteo where date(fecha) = ? ",[new Date()])
        if(found) {
            result.message = 'Los sectores ya han sido cargados'
            respond(result, status: 200)
            return 
        }

        def username = getPrincipal().username
        def today = new Date()
       
        def sectores = Sector.list([sort:'sectorFolio', order:'asc']);
        def conteos = [];
        sectores.each { sector ->
            Conteo conteo = Conteo.where{ sector == sector && fecha == today}.find()
             // println "Encontro conteo para sector ${sector.sectorFolio} y fecha ${today}"
            if( !conteo ){
               // println "No encontro conteo para sector ${sector.sectorFolio} y fecha ${today}"
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
        result.message = existencias.size()+" exitencias generadas exitosamente"
        result.existencias = existencias.size()
        respond(result, status: 200)
    }

    @Transactional
    def generarExistenciaParcial(Sucursal sucursal) {

        println "Generando existencia parcial... "

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

        def conteosDet = ConteoDet.findAll()
        def ejercicio = hoy[Calendar.YEAR]
        def mes = hoy[Calendar.MONTH] + 1

        def existencias = []

        conteosDet.each { det ->       
            def existencia = Existencia.where{sucursal == sucursal && anio == ejercicio && mes == mes && producto == det.producto }.find()
            if(existencia){
                def ex = new ExistenciaConteo()
                ex.existencia = existencia
                ex.sucursal = existencia.sucursal
                ex.producto = existencia.producto
                ex.fecha = hoy
                ex.cantidad = existencia.cantidad
                ex.save failOnError: true , flush:true
                existencias.add(ex)
            }
        }

        result.message = existencias.size()+" exitencias generadas exitosamente"
        result.existencias = existencias.size()
        respond(result, status: 200)

    }

    @Transactional
    def limpiarExistencias(Sucursal sucursal){
        def hoy = new Date()
       
       def existencias = Existencia.findAll()
       def conteos = Conteo.findAll()


        // ConteoDet.deleteAll()
        Map result = [:]
        result.message = "Tablas  Limmpias "
        respond(result, status: 200)
    }

    def print() {
        params.SECTOR = params.id
        println params.id
        def pdf = this.reportService.run('ConteoAlmacen', params)
        def fileName = "ConteoAlmacen.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteNoCapturados(Sucursal sucursal) {
        params.SUCURSAL_ID = sucursal.id
        println params.id
        def pdf = this.reportService.run('NoCapturados', params)
        def fileName = "NoCapturados.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteValidacion(){

        println "----------------------- Ejecutando Validacion"

        def fecha  = new Date()
        def repParams = [:]

        repParams.SUC = params.sucursalId
        repParams.FECHA = fecha
        repParams.SECTOR_INICIAL = new Integer(params.sectorIni)
        repParams.SECTOR_FINAL = new Integer(params.sectorFin)
        repParams.DIF_DE = new Double(params.difDe)
        repParams.DIF_A = new Double(params.difA)

        switch(params.activo) {
            case 'TODOS':
                repParams.ACTIVO = ''
            break
            case 'ACTIVOS':
                repParams.ACTIVO = " AND ACTIVO IS TRUE"
            break
            case 'INACTIVOS':
                repParams.ACTIVO = " AND ACTIVO IS FALSE"
            break
            default:
                repParams.ACTIVO = ''
            break
        }

        switch(params.deLinea) {
            case 'TODOS':
                repParams.DELINEA = ''
            break
            case 'DE LINEA':
                repParams.DELINEA = " AND DE LINEA IS TRUE"
            break
            case 'ESPECIALES':
                repParams.DELINEA = " AND DE LINEA IS FALSE"
            break
            default:
                repParams.DELINEA = ''
            break
        }

        switch(params.filtro) {
            case 'TODOS':
                repParams.FILTRO = "LIKE '%' "
            break
            case 'SIN DIFERENCIA':
                repParams.FILTRO = " = 0 "
            break
            case 'CON DIFERENCIA':
                repParams.FILTRO = " <> 0 "
            break
            default:
                repParams.FILTRO = "LIKE '%' "
            break
        }

        switch(params.ordenado) {
            case 'CLAVE':
                repParams.ORDENADOR = " L.LINEA, CL.CLASE,P.CLAVE "
            break
            case 'NOMBRE':
                repParams.ORDENADOR = " L.LINEA, CL.CLASE,P.DESCRIPCION "
            break
            case 'DIFERENCIA ASC':
                repParams.ORDENADOR = " 11 ASC "
            break
            case 'DIFERENCIA DESC':
                repParams.ORDENADOR = " 11 DESC "
            break
            default:
                
            break
        }

        def pdf = this.reportService.run('ValidacionDeCaptura', repParams)
        def fileName = "Validacion.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def reporteDiferencias(){

        println "************************Ejecutando Diferencias"

        def fecha  = new Date()
        def repParams = [:]

        repParams.SUC = params.sucursalId
        repParams.FECHA = fecha
        repParams.SECTOR_INICIAL = new Integer(params.sectorIni)
        repParams.SECTOR_FINAL = new Integer(params.sectorFin)
        repParams.DIF_DE = new Double(params.difDe)
        repParams.DIF_A = new Double(params.difA)

        switch(params.activo) {
            case 'TODOS':
                repParams.ACTIVO = ''
            break
            case 'ACTIVOS':
                repParams.ACTIVO = " AND ACTIVO IS TRUE"
            break
            case 'INACTIVOS':
                repParams.ACTIVO = " AND ACTIVO IS FALSE"
            break
            default:
                repParams.ACTIVO = ''
            break
        }

        switch(params.deLinea) {
            case 'TODOS':
                repParams.DELINEA = ''
            break
            case 'DE LINEA':
                repParams.DELINEA = " AND DE LINEA IS TRUE"
            break
            case 'ESPECIALES':
                repParams.DELINEA = " AND DE LINEA IS FALSE"
            break
            default:
                repParams.DELINEA = ''
            break
        }

        switch(params.filtro) {
            case 'TODOS':
                repParams.FILTRO = "LIKE '%' "
            break
            case 'SIN DIFERENCIA':
                repParams.FILTRO = " = 0 "
            break
            case 'CON DIFERENCIA':
                repParams.FILTRO = " <> 0 "
            break
            default:
                repParams.FILTRO = "LIKE '%' "
            break
        }

        switch(params.ordenado) {
            case 'CLAVE':
                repParams.ORDENADOR = " L.LINEA, CL.CLASE,P.CLAVE "
            break
            case 'NOMBRE':
                repParams.ORDENADOR = " L.LINEA, CL.CLASE,P.DESCRIPCION "
            break
            case 'DIFERENCIA ASC':
                repParams.ORDENADOR = " 11 ASC "
            break
            case 'DIFERENCIA DESC':
                repParams.ORDENADOR = " 11 DESC "
            break
            default:
                
            break
        }

        def pdf = this.reportService.run('DiferenciasEnConteo', repParams)
        def fileName = "Diferencias.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def imprimirSectores() {

        println "Imprimiendo Sectores para conteo"

        //def fecha  = new Date('08/20/2019')
        def fecha  = new Date()

        def conteos = Conteo.findAll("from Conteo where date(fecha) = ? ",[fecha])

        if(!conteos){
            println "No hubo conteos para imprimir"
            return[]
        }
        println conteos
        def pdf = reportService.imprimirSectoresConteo('ConteoAlmacen.jrxml', conteos)
        def fileName = "ConteosAlmacen.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
       
    }

    def fijarConteo() {
        println "Fijando el conteo"
        def result = [:]
        def found = ExistenciaConteo.findAll("from ExistenciaConteo where date(fijado) = ? ",[new Date()])
        if(found) {
            result.message = 'El inventario ya ha sido fijado'
            respond(result, status: 200)
            return 
        }
        conteoService.fijarConteo()
        result.message = 'El inventario se fijo con exito'
        respond(result, status: 200)
        return 
    }

    def ajustePorConteo() {
        def aju = conteoService.ajustePorConteo()
        def result = [:]
        def mensaje ="Se genero el ajuste con Documento: " +aju.documento +" para "+ aju.partidas.size()+" productos"
        if(aju) {
            result.message = mensaje
            respond(result, status: 200)
            return 
        }

        return
    }

    def cargarSector() {
        println 'Cargando Sector ...'
        
        println params

        def result = [:]
        def username = getPrincipal().username
        def today = new Date()

        def found = Conteo.findByDocumento(params.sector)

        if(found) {
            result.message = 'Sector ya cargado'
            respond(result, status: 200)
            return 
        }  

        def sector = Sector.findBySectorFolio(params.sector)

        if(sector){

            def conteo = new Conteo([
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

            respond conteo

        }

        return []
    }
    

}


