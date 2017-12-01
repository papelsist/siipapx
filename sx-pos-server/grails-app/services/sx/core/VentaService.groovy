package sx.core

import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.cxc.CuentaPorCobrar
import lx.cfdi.v33.CfdiUtils


import com.luxsoft.cfdix.v33.CfdiFacturaBuilder

@Transactional
class VentaService {

    CfdiService cfdiService

    CfdiTimbradoService cfdiTimbradoService

    SpringSecurityService springSecurityService

    @Publisher
    def save(Venta venta) {
        fixCortes(venta)
        fixNombre(venta)
        logEntity(venta)
        fixVendedor(venta)
        if(venta.id == null){
            Folio folio=Folio.findOrCreateWhere(entidad: 'VENTAS', serie: 'PEDIDOS')
            def res = folio.folio + 1
            // def res = folio.next()
            folio.folio = res
            log.debug('Utilizando folio: {}', folio)
            log.debug('Asignando folio: {}', res)
            venta.documento = res
            folio.save()
        }
        venta.save()
        return venta
    }

    /**
     * Arregla un aparente error en el mapeo de entidades VentaDet y Corte
     *
     * @TODO: Arreglar esto en el mapeo
     *
     * @param venta
     * @return
     */
    private fixCortes(Venta venta) {
        venta.partidas.each {
            if(it.corte)
                it.corte.ventaDet = it;
        }
    }

    /**
     * Actualiza el nombre en la venta cuando se trata de ventas de mostrador
     *
     * @param venta
     * @return
     */
    private fixNombre(Venta venta) {
        if(venta.cliente.rfc != 'XAXX010101000')
            venta.nombre = venta.cliente.nombre
    }

    private fixVendedor(Venta venta) {
        if(venta.vendedor == null) {
            venta.vendedor = Vendedor.findByNombres('CASA')
        }
        assert venta.vendedor, 'No fue posible asignar vendedor a la venta'
    }

    /**
     * Actualiza la columna de facturar para indicar que la venta esta lista para ser facurada en el area de caja
     * publicando un evento con el nombre del metodo. Es importante no cambiar el nombre de este metodo ya que es el id
     * del evento que otros servicios podrian estar escuchando (de manera asincrona)
     *
     * @param venta
     * @return
     */
    @Publisher
    def mandarFacturar(String ventaId) {
        Venta venta = Venta.get(ventaId)
        log.debug('Mandando facturar venta......'+ venta.getFolio())
        venta.facturar = new Date()
        venta.save()
        return venta
    }

    @Publisher
    def facturar(Venta pedido) {
        log.debug("Facturando  ${pedido.statusInfo()}")
        pedido = generarCuentaPorCobrar(pedido)
        // generarCfdi(pedido)
        return pedido
    }

    def generarCuentaPorCobrar(Venta pedido) {
        if (pedido.cuentaPorCobrar == null) {
            CuentaPorCobrar cxc = new CuentaPorCobrar()
            cxc.sucursal = pedido.sucursal
            cxc.cliente = pedido.cliente
            cxc.tipoDocumento = 'VENTA'
            cxc.importe = pedido.importe
            cxc.impuesto = pedido.impuesto
            cxc.total  = pedido.total
            cxc.formaDePago = pedido.formaDePago
            cxc.moneda = pedido.moneda
            cxc.tipoDeCambio = pedido.tipoDeCambio
            cxc.comentario = pedido.comentario
            cxc.tipo = pedido.cod ? 'COD': pedido.tipo
            cxc.documento = Folio.nextFolio('FACTURAS',cxc.tipo)
            cxc.fecha = new Date()
            cxc.createUser = pedido.createUser
            cxc.updateUser = pedido.updateUser
            cxc.comentario = 'GENERACION AUTOMATICA'
            pedido.cuentaPorCobrar = cxc
            cxc.save failOnError: true
            pedido.cuentaPorCobrar = cxc
            pedido.save flush: true
        }
    }

    def generarCfdi(Venta venta){
        assert venta.cuentaPorCobrar, " La venta ${venta.documento} no se ha facturado"
        CfdiFacturaBuilder builder = new CfdiFacturaBuilder();
        def comprobante = builder.build(venta)
        // println CfdiUtils.serialize(comprobante)
        def cfdi = cfdiService.generarCfdi(comprobante, 'I')
        venta.cuentaPorCobrar.cfdi = cfdi
        venta.save flush: true
        return cfdi
    }

    def timbrar(Venta venta){
        assert venta.cuentaPorCobrar, "La venta ${venta} no se ha facturado"
        assert !venta.cuentaPorCobrar?.cfdi?.uuid, "La venta ${venta} ya esta timbrada "
        def cfdi = venta.cuentaPorCobrar.cfdi
        if (cfdi == null) {
            cfdi = generarCfdi(venta)
        }
        cfdi = cfdiTimbradoService.timbrar(cfdi)
        return cfdi;
    }


    def logEntity(Venta venta) {
        def user = getUser()
        if(! venta.id)
            venta.createUser = user
        venta.updateUser = user
    }

    def getUser() {
        def principal = springSecurityService.getPrincipal()
        return principal.username
    }

    def getFolio() {
        return Folio.nextFolio('VENTAS','PEDIDOS')
    }

}
