package sx.tesoreria

import com.luxsoft.utils.Periodo
import grails.rest.*
import grails.converters.*

class CorteDeTarjetaController extends RestfulController {

    static responseFormats = ['json']

    CorteDeTarjetaController() {
        super(CorteDeTarjeta)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('List: {}', params)
        println ''
        return super.listAllResources(params)
    }


}
