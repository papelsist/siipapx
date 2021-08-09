package sx.contabilidad

import grails.rest.RestfulController
import grails.web.http.HttpHeaders

import static org.springframework.http.HttpStatus.CREATED

class CuentaContableController extends RestfulController{

    static responseFormats = ['json']

    public CuentaContableController(){
        //super(CuentaContable)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = CuentaContable.where {}
        params.max = 1000
        if(params.nivel){
            log.info('Buscando cuentas de detalle...'+ params)
            query = query.where {detalle == true}
        }
        if(params.term){
            def search = params.term + '%'
            query = query.where { clave =~ search || descripcion =~ search}
            params.max = 100
            return query.list(params)
        }
        return query.list(params)
    }

    @Override
    protected Object queryForResource(Serializable id) {
        //return super.queryForResource(id)
        return CuentaContable.findById(id, [fetch:[subCuentas:"join", cuentaSat:"join"]])
    }

    /*@Override
    protected Object saveResource(Object resource) {
        log.info('Salvando cuenta: ' + resource)
        return super.saveResource(resource)
    }

    @Override
    protected Object getObjectToBind() {
        log.info('Object to bind: ' + request)
        return request
    }*/
/*
    @Override
    def save() {
        if(handleReadOnly()) {
            return
        }
        def instance = createResource()

        instance.validate()
        if (instance.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond instance.errors, view:'create' // STATUS CODE 422
            return
        }

        saveResource instance

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: "${resourceName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        grailsLinkGenerator.link( resource: this.controllerName, action: 'listaDePreciosVenta.show',id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null ))
                respond instance, [status: CREATED, view:'listaDePreciosVenta.show']
            }
        }
    }
    */
}


