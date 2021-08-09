package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SocioController extends RestfulController{

    static responseFormats = ['json']

    public SocioController(){
        super(Socio)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Socio.where {cliente.id == params.clienteId}
        return query.list([sort: 'nombre', orde: 'asc'])
    }



}
