package sx.cxc

import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController
import groovy.util.logging.Slf4j

@GrailsCompileStatic
@Slf4j
class AplicacionDeCobroController extends RestfulController<AplicacionDeCobro>{

    AplicacionDeCobroService aplicacionDeCobroService

    AplicacionDeCobroController() {
        super(AplicacionDeCobro)
    }

    @Override
    protected List<AplicacionDeCobro> listAllResources(Map params) {
        def cobroId = params.cobroId
        return AplicacionDeCobro.where{ cobro.id == cobroId}.list()
    }

    @Override
    protected AplicacionDeCobro saveResource(AplicacionDeCobro resource) {
        return aplicacionDeCobroService.save(resource)
    }

    @Override
    protected AplicacionDeCobro updateResource(AplicacionDeCobro resource) {
        return aplicacionDeCobroService.save(resource)
    }
}
