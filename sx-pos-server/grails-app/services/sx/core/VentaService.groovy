package sx.core

import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
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
        // fixEnvio(venta)
        fixNombre(venta)
        logEntity(venta)
        fixVendedor(venta)
        fixDescuentoOriginal(venta)
        fixSinExistencias(venta)
        if(venta.id == null){
            Folio folio=Folio.findOrCreateWhere(entidad: 'VENTAS', serie: 'PEDIDOS')
            def res = folio.folio + 1
            folio.folio = res
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

    private fixEnvio(Venta venta) {
        if(!venta.id && venta.envio) {
            if (!venta.envio.venta)
                venta.envio.venta = venta;
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

    private fixDescuentoOriginal(Venta venta) {
        def desc = null;
        if(venta.tipo == 'CRE') {
            venta.partidas.each {it.descuentoOriginal = it.descuento}
            venta.descuentoOriginal = venta.descuento
        }
    }
    private fixSinExistencias(Venta venta) {
        def sinExistencias = venta.partidas.find { it.sinExistencia}
        venta.sinExistencia = sinExistencias != null
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
        assert pedido.cuentaPorCobrar == null, "Pedido${pedido.getFolio()} ya facturado : ${pedido.statusInfo()}"
        pedido = generarCuentaPorCobrar(pedido)
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
        log.debug('Generando CFDI para  {}', venta.statusInfo())
        CfdiFacturaBuilder builder = new CfdiFacturaBuilder();
        def comprobante = builder.build(venta)
        def cfdi = cfdiService.generarCfdi(comprobante, 'I')
        venta.cuentaPorCobrar.cfdi = cfdi
        venta.save flush: true
        return cfdi
    }

    def timbrar(Venta venta){
        log.debug("Timbrando  {}", venta.statusInfo());
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
        /*
        def user = getUser()
        if(! venta.id)
            venta.createUser = user
        venta.updateUser = user
        */
    }

    def getUser() {
        def principal = springSecurityService.getPrincipal()
        return principal.username
    }

    def getFolio() {
        return Folio.nextFolio('VENTAS','PEDIDOS')
    }

    def cancelar(Venta venta) {
        if(!venta.cuentaPorCobrar) {
            respond venta
            return
        }

        CuentaPorCobrar cxc = venta.cuentaPorCobrar

        // Eliminando los pagos
        if(cxc && cxc.pagos) {
            log.debug('Eliminando aplicaciones de cobro')
            def aplicaciones = AplicacionDeCobro.where{ cuentaPorCobrar == cxc}.list()
            aplicaciones.each { AplicacionDeCobro a ->
                Cobro cobro = a.cobro
                cobro.removeFromAplicaciones(a)
                cobro.save()
            }
        }
        venta.cuentaPorCobrar = null
        venta.save()
        /*
        if(cxc.cfdi) {
            Cfdi cfdi = cxc.cfdi
            cxc.cfdi = null
            cfdiService.cancelar(cfdi)
        }
        log.debug('Eliminando la cuenta por cobrar')
        cxc.delete ()
        */
        return venta
    }

}
