package sx.cxc

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import grails.web.http.HttpHeaders
import sx.core.Folio
import static org.springframework.http.HttpStatus.OK

import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_CXC_USER')")
class NotaDeCreditoController extends RestfulController{

    static responseFormats = ['json']

    NotaDeCreditoController(){
        super(NotaDeCredito)
    }

    
    @Override
    protected Object saveResource(Object resource) {
        if(resource.id == null)
            resource.folio = Folio.nextFolio(resourceName, resource.serie)
        resource.save flush:true
    }

    @Transactional
    def update() {
        if(handleReadOnly()) {
            return
        }
        NotaDeCredito notaInstance = NotaDeCredito.get(params.id)
        if (notaInstance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        notaInstance.properties = getObjectToBind()
        log.info('Actualizando nota de credito: ' + notaInstance)

        if (notaInstance.hasErrors()) {
            log.info('Errores de validacion: ' + notaInstance)
            transactionStatus.setRollbackOnly()
            respond notaInstance.errors, view:'edit' // STATUS CODE 422
            return
        }
        notaInstance.save flush:true
        log.info ("Nota salvada: "+ notaInstance.id)

        response.addHeader(HttpHeaders.LOCATION,
                grailsLinkGenerator.link( resource: this.controllerName, action: 'show',id: notaInstance.id, absolute: true,
                        namespace: hasProperty('namespace') ? this.namespace : null ))
        respond notaInstance, [status: OK]
    }



}
