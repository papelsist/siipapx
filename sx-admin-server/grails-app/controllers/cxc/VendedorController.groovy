package cxc

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

import sx.core.Vendedor

@Secured("hasRole('CXC_USER')")
class VendedorController extends RestfulController<Vendedor>{
    static responseFormats = ['json']

    VendedorController(){
        super(Vendedor);
    }

    @Override
    protected List<Vendedor> listAllResources(Map params) {
        params.max = 500;
        return super.listAllResources(params)
    }
}
