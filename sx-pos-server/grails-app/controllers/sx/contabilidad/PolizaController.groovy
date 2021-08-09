package sx.contabilidad

import grails.rest.RestfulController
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.commons.lang.builder.ToStringStyle

class PolizaController extends RestfulController{

    static responseFormats = ['json']

    def polizaService

    public PolizaController(){
       // super(Poliza)
    }



    @Override
    protected List listAllResources(Map params) {

        def query = Poliza.where {}

        if(params.subtipo && params.subtipo != 'Todas') {
            query = query.where {subtipo == params.subtipo}
        }

        if(params.ejercicio && params.mes) {

            query = query.where {ejercicio == params.ejercicio && mes == params.mes}
        }

        return query.list(params)
    }

    /*
    @Override
    protected Object createResource() {
        return super.createResource()

//        def json = request.JSON
//        Poliza instance = new Poliza()
//        instance.subTipo = sub
//        bindData instance, json
//        instance
//        return instance

    }
    */

    @Override
    protected Object saveResource(Object resource) {
        polizaService.save(resource)
    }
}
