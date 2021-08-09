package sx.inventario

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class SectorController extends RestfulController {

    static responseFormats = ['json']

    ReportService reportService

    SectorController() {
        super(Sector)
    }

    @Override
    protected List listAllResources(Map params) {
        println 'Buscando sercores ' + params
        log.debug('Buscando: {} ', params)
        params.sort = 'sectorFolio'
        params.order = 'asc'
        params.max = 1000
        def query = Sector.where {}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { sectorFolio == params.term.toInteger() }
            } else {
                query = query.where { comentario =~ search || responsable1 =~ search  || responsable2 =~ search}
            }
        }
        def list = query.list(params)

        return list
    }
    
    @Secured("ROLE_INVENTARIO_USER")
    // @Override
    protected Sector saveResource(Sector resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.createUser = username
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }

    protected Sector updateResource(Sector resource) {
        resource.partidas = resource.partidas.sort { it.indice}
        def username = getPrincipal().username
        resource.updateUser = username
        return super.updateResource(resource)
    }

    def print() {
        params.SECTOR = params.id
        def pdf = this.reportService.run('SectorAlmacen', params)
        def fileName = "SectorAlmacen.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def productosSinSector() {
        params.SUCURSAL = AppConfig.first().sucursal.id
        def pdf = this.reportService.run('ProductosSinSector', params)
        def fileName = "ProductosSinSector.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def recorridosPorLinea() {
        log.debug('Params: {}', params)
        def repParams = [:]
        repParams.LINEA = params.linea
        repParams.CLASE = params.clase
        // Estado
        switch (params.estado) {
            case 'TODOS':
                repParams.ACTIVO = ' '
                break;
            case 'ACTIVOS':
                repParams.ACTIVO = ' AND ACTIVO IS TRUE'
                break;
            case 'INACTIVOS':
                repParams.ACTIVO = ' AND ACTIVO IS FALSE'
                break;
        }
        // Tipo
        switch (params.tipo) {
            case 'TODOS':
                repParams.DELINEA = ' '
                break;
            case 'DELINEA':
                repParams.DELINEA = ' AND DE_LINEA IS TRUE'
                break;
            case 'ESPECIALES':
                repParams.DELINEA = ' AND DE_LINEA IS FALSE'
                break;
        }
        //existencia = ['TODOS', 'POSITIVOS', 'DEGATIVOS', 'EN_CERO'];
        switch (params.existencia) {
            case 'TODOS':
                repParams.EXISTENCIA = ' '
                break;
            case 'POSITIVOS':
                repParams.EXISTENCIA = ' AND E.CANTIDAD > 0'
                break;
            case 'NEGATIVOS':
                repParams.EXISTENCIA = ' AND E.CANTIDAD < 0'
                break;
            case 'EN_CERO':
                repParams.EXISTENCIA = ' AND E.CANTIDAD == 0'
                break;
        }
        // params.SUCURSAL = AppConfig.first().sucursal.id
        if (!params.clase) {
            repParams.CLASE = '%'
        }
        if (!params.linea) {
            repParams.LINEA = '%'
        }

        def pdf = this.reportService.run('RecorridosPorLinea', repParams)
        def fileName = "ProductosSinSector.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }



}


