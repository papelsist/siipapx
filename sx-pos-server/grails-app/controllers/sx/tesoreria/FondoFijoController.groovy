package sx.tesoreria

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_POS_USER')")
class FondoFijoController extends RestfulController {

    static responseFormats = ['json']

    FondoFijoController() {
      super(FondoFijo)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'fecha'
        params.order = 'asc'
        params.max = 100
        def query = FondoFijo.where {}
        return query.list(params)
    }

    def fondos(FondosPorFechaCommand command) {
        log.debug('FondosFijos por {}', command)
        params.sort = 'fecha'
        params.order = 'asc'
        params.max = 100
        def list = FondoFijo.executeQuery("from FondoFijo f where date(f.fecha)=? ", command.fecha)
        respond list
    }

    def pendientes() {
        log.debug('FondosFijos pendientes',)
        params.sort = 'fecha'
        params.order = 'asc'
        params.max = 100
        def list = FondoFijo.executeQuery("from FondoFijo f where f.fondo is null and rembolso = false")
        respond list
    }

    def prepararRembolso() {
        FondoFijo fondoFijo = new FondoFijo()
        def importe = FondoFijo
                .executeQuery("select sum (f.importe) from FondoFijo f " +
                " where f.solicitado!=null and f.rembolso = false and fondo is null")[0]?:00
        fondoFijo.importe = importe
        respond fondoFijo
    }
    protected FondoFijo saveResource(FondoFijo resource) {
        def username = getPrincipal().username
        if(!resource.rembolso) {
            resource.importe = resource.importe.abs() * -1
        }
        resource.createUser = username
        resource.updateUser = username
        if(resource.rembolso) {
            resource.save failOnError: true, flush: true
            def gastos = FondoFijo
                    .executeQuery("from FondoFijo f " +
                    " where f.solicitado!=null and f.rembolso = false and fondo is null")
            gastos.each{
                it.fondo = resource
                it.save flush: true
            }

        }
        return super.saveResource(resource)
    }

    protected FondoFijo updateResource(FondoFijo resource) {
        resource.updateUser = getPrincipal().username
        return super.updateResource(resource)
    }

    def solicitarRembolso(SolicitudDeRembolsoCommand command){
        log.debug('Solicitando rembolso con {}', command)
        command.gastos.each {
            if(!it.rembolso && !it.solicitado) {
                it.solicitado = new Date()
                it.save failOnError: true, flush:true
            }
        }
        respond command
    }


}

class SolicitudDeRembolsoCommand {
    List<FondoFijo> gastos

    String toString() {
        "Gasgos ${gastos? gastos.size() : 0}"
    }
}

class FondosPorFechaCommand {
    Date fecha

    String toString() {
        return "${fecha.format('dd/MM/yyyy')}"
    }
}