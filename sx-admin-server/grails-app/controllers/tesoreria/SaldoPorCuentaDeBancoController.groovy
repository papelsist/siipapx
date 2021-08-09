package sx.tesoreria


import grails.rest.*
import grails.converters.*

class SaldoPorCuentaDeBancoController extends RestfulController {
    static responseFormats = ['json', 'xml']
    SaldoPorCuentaDeBancoController() {
        super(SaldoPorCuentaDeBanco)
    }
}
