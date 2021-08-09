package sx.pos.server

import com.luxsoft.utils.Periodo
import grails.compiler.GrailsCompileStatic

/**
 * Obtiene el period del HttpParms de no ser posible genera uno por defualt
 *
 */
@GrailsCompileStatic
class PeriodoInterceptor {


    public PeriodoInterceptor(){
        match controller: 'pedido'
    }

    boolean before() {
        //log.debug('Buscando periodo {}', params)
        if(params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            params.periodo = periodo
        }
        true
    }

}
