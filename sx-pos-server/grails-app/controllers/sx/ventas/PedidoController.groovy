package sx.ventas

import com.luxsoft.utils.Periodo
import grails.rest.RestfulController
import sx.core.Venta

class PedidoController extends RestfulController {

    static responseFormats = ['json']

    PedidoController() {
        super(Venta)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('List {}', params)
        params.max = Math.min(params.max ?: 10, 100)
        params.sort = params.sort?: 'lastUpdated'
        params.order = params.order?: 'desc'

        def query = Venta.where {cuentaPorCobrar == null && facturar == null && sucursal == params.sucursal}
        if(params.periodo) {
            // log.debug('Tratando de resolver: {}', params.periodo)
            Periodo periodo = params.periodo
            query = query.where { fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }
        if(params.cliente) {
            String search = '%' + params.cliente + '%'
            query = query.where { nombre =~ search  }
        }

        if (params.tipo == 'CREDITO') {
            query = query.where {facturar !=  null  && cuentaPorCobrar == null}
            if(params.facturables == 'CRE'){
                query = query.where {tipo == params.facturables}
            } else {
                query = query.where {tipo != 'CRE'}
            }
        }
        
        if( params.term && params.term.isInteger()){
            // log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { documento == params.documento.toInteger() }
        } else  if( params.term) {
            // String search = '%' + params.term + '%'
            // query = query.where { nombre =~ search  }
        }

        return query.list(params)
    }
}

class PedidosPendientesFilter {

}
