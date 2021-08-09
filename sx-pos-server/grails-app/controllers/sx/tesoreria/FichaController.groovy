package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Sucursal
import sx.reportes.PorFechaCommand

@Secured("hasRole('ROLE_CXC_USER')")
class FichaController extends RestfulController {

    static responseFormats = ['json']

    FichaController() {
        super(Ficha)
    }

    @Override
    protected List listAllResources(Map params) {

        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        PorFechaCommand command = new PorFechaCommand()
        bindData(command, params)
        def query = Ficha.where {fecha == command.fecha}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        log.debug('Cargando fichas {}', command)
        return query.list(params)
    }
}

