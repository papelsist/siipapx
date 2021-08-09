package sx.cxc

import com.luxsoft.cfdix.v33.V33CfdiUtils
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Sucursal
import sx.core.Venta

@Secured("hasRole('ROLE_POS_USER')")
class CuentaPorCobrarController extends RestfulController{

    static responseFormats = ['json']

    CuentaPorCobrarController() {
        super(CuentaPorCobrar)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = CuentaPorCobrar.where {}
        params.sort = params.sort ?:'documento'
        params.order = params.order ?:'desc'
        if(params.boolean('canceladas')){
            query = query.where {cancelada != null}
        }
        if(params.documento){
          int documento = params.int('documento')
          query = query.where { documento >= documento }
        }
        if(params.cliente){
            query = query.where { cliente.id == params.cliente}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            } else {
                log.debug(' Buscando por cliente: {}', search)
                query = query.where { cliente.nombre =~ search }
            }
        }
        return query.list(params)
    }

    def pendientesCod(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = 100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def rows = Venta.findAll("from Venta c  where c.cuentaPorCobrar.total - c.cuentaPorCobrar.pagos > 0 and c.cuentaPorCobrar.tipo = ? order by c.dateCreated desc ", ['COD'])
        respond rows
    }

    def canceladas(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = CuentaPorCobrar.where{ sucursal == sucursal && cancelada != null}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            } else {
                query = query.where { cliente.nombre =~ search }
            }
        }
        respond query.list(params)
    }

    def buscarVenta(CuentaPorCobrar cxc) {
        // log.debug('Buscando venta origen {}', cxc)
        if (cxc== null) {
            notFound()
            return
        }
        def venta = Venta.where {cuentaPorCobrar == cxc}.find()
        respond venta
    }

    def buscarPartidas(CuentaPorCobrar cxc){
        log.debug('Buscando partidas originales {}', params)
        assert cxc.cfdi, 'No esta timbrada'
        respond V33CfdiUtils.getPartidas(cxc.cfdi)
    }

}
