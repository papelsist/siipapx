package sx.crm


import grails.rest.*
import grails.converters.*

class BonificacionMCAplicacionController extends RestfulController<BonificacionMCAplicacion> {
    static responseFormats = ['json', 'xml']
    BonificacionMCAplicacionController() {
        super(BonificacionMCAplicacion)
    }

    @Override
    protected BonificacionMCAplicacion saveResource(BonificacionMCAplicacion resource) {
        String proveedorId = params.proveedorId
        return super.saveResource(resource)
    }
}
