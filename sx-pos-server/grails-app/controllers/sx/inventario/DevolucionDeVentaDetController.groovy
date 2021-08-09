package sx.inventario


import grails.rest.*
import grails.converters.*

class DevolucionDeVentaDetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    DevolucionDeVentaDetController() {
        super(DevolucionDeVentaDet)
    }
}
