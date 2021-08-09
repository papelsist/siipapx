package sx.inventario


import grails.rest.*
import grails.converters.*

class TransformacionDetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    TransformacionDetController() {
        super(TransformacionDet)
    }
}
