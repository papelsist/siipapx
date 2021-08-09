package sx.inventario


import grails.rest.*
import grails.converters.*

class MovimientoDeAlmacenDetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    MovimientoDeAlmacenDetController() {
        super(MovimientoDeAlmacenDet)
    }
}
