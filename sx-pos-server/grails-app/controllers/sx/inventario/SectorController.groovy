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

        params.sort = 'sectorFolio'
        params.order = 'asc'
        params.max = 1000
        def query = Sector.where {}
        if(params.documento) {
            def documento = params.int('documento')
            query = query.where {sectorFolio >=  documento}
        }
        return query.list(params)
    }

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



}


