package sx.tesoreria

import grails.rest.RestfulController
import sx.tesoreria.MovimientoDeTesoreria

class MovimientoDeTesoreriaController extends  RestfulController<MovimientoDeTesoreria>{

    static responseFormats = ['json']

    MovimientoDeTesoreriaService movimientoDeTesoreriaService

    MovimientoDeTesoreriaController(){
        super(MovimientoDeTesoreria)
    }


    protected MovimientoDeTesoreria saveResource(MovimientoDeTesoreria resource) {
        return movimientoDeTesoreriaService.save(resource);
    }


    protected MovimientoDeTesoreria createResource() {
        MovimientoDeTesoreria movimientoDeTesoreria =  super.createResource()
        movimientoDeTesoreriaService.registrarIngreso(movimientoDeTesoreria)
        return movimientoDeTesoreria
    }
}
