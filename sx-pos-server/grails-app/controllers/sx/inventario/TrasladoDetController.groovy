package sx.inventario


import grails.rest.*
import grails.converters.*

class TrasladoDetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    TrasladoDetController() {
        super(TrasladoDet)
    }
}
