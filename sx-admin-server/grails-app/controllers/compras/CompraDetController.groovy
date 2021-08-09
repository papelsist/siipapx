package sx.compras

import grails.rest.RestfulController

class CompraDetController extends RestfulController{

    static responseFormats = ['json']

    CompraDetController(){
        super(CompraDet)
    }
}
