package sx.tesoreria


import grails.rest.*
import grails.converters.*

class InversionController extends RestfulController {
    static responseFormats = ['json', 'xml']
    InversionController() {
        super(Inversion)
    }
}
