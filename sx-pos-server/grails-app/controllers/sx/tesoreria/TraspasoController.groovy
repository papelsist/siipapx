package sx.tesoreria


import grails.rest.*
import grails.converters.*

class TraspasoController extends RestfulController {
    static responseFormats = ['json', 'xml']
    TraspasoController() {
        super(Traspaso)
    }
}
