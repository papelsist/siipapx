package sx.tesoreria


import grails.rest.*
import grails.converters.*

class ChequeController extends RestfulController {
    static responseFormats = ['json', 'xml']
    ChequeController() {
        super(Cheque)
    }
}
