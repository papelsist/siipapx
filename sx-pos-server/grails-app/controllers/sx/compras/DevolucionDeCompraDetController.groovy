package sx.compras


import grails.rest.*
import grails.converters.*

class DevolucionDeCompraDetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    DevolucionDeCompraDetController() {
        super(DevolucionDeCompraDet)
    }
}
