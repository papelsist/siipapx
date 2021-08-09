package sx.cxc

import com.luxsoft.utils.Periodo
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import sx.core.AppConfig
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class AnticipoController extends RestfulController{

    static responseFormats = ['json']

    ReportService reportService

    AnticipoController(){
        super(Cobro)
    }


    protected Object createResource() {
        Cobro instance = new Cobro()
        instance.anticipo = true
        instance.tipo = 'COD'
        instance.sucursal = AppConfig.first().sucursal
        bindData instance, getObjectToBind()
        instance
    }


    protected Object saveResource(Object resource) {
        log.debug('Salvando registro: {}', resource)
        resource.save failOnError: true, flush: true
        log.debug('Anticipo salvado: {} ', resource)
    }

    protected List listAllResources(Map params) {
        log.debug('List {}', params)
        params.max = Math.min(params.max ?: 10, 100)
        params.sort = params.sort?: 'fecha'
        params.order = params.order?: 'desc'

        def query = Cobro.where {anticipo == true && tipo != 'CRE'}

        if(params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            query = query.where { fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }


        if( params.term ){
            String search = '%' + params.term + '%'
            query = query.where { cliente.nombre =~ search  }
        }
        /*
        if(params.getBoolean('pendientes')){

        }
        */
        return query.list(params)
    }

    def print(){
        Map repParams = [:]
        repParams['SUCURSAL'] = AppConfig.first().sucursal
        repParams['ID'] = params.ID
        def pdf = reportService.run('AnticipoDeClientes', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "AnticiposDeClientes.pdf")
    }
}
