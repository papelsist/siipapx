package sx.core

import com.luxsoft.utils.MonedaUtils
import grails.events.EventPublisher
import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.lang3.time.DateUtils

import sx.cfdi.Cfdi
import sx.cfdi.CfdiPdfService
import sx.cfdi.CfdiService
import sx.cfdi.CfdiPdfService
import sx.cfdi.CfdiTimbradoService
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar
import lx.cfdi.v33.CfdiUtils
import sx.cloud.LxPedidoService


import com.luxsoft.cfdix.v33.CfdiFacturaBuilder
import sx.inventario.InventarioService
import sx.cxc.AnticipoSatService

@Transactional
class VentaService implements  EventPublisher{

    CfdiService cfdiService

    CfdiFacturaBuilder cfdiFacturaBuilder

    CfdiTimbradoService cfdiTimbradoService

    SpringSecurityService springSecurityService

    InventarioService inventarioService

    LxPedidoService lxPedidoService

    CfdiPdfService cfdiPdfService

    AnticipoSatService anticipoSatService

    @Publisher
    def save(Venta venta) {
        if(venta.tipo == 'ANT'){
            return saveFacturaDeAnticipo(venta)
        }
        fixCortes(venta)
        fixDescuentos(venta)
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

    def saveFacturaDeAnticipo(Venta venta) {
        fixNombre(venta)
        fixVendedor(venta)
        if(venta.id == null){
            Folio folio=Folio.findOrCreateWhere(entidad: 'VENTAS', serie: 'ANTICIPOS')
            def res = folio.folio + 1
            folio.folio = res
            venta.documento = res
            folio.save()
        }
        // Generar partida de anticipo
        /*
        VentaDet det = new VentaDet()
        det.producto = Producto.where{}
        */
        venta.save()
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

    private fixDescuentos(Venta venta) {
        venta.partidas.each {
            if(it.corte) {
                def descuentoGeneral = venta.descuento
                def descuento = it.descuento
                def factor = it.producto.unidad == 'MIL' ? 1000.00 : 1.00
                def importe = (it.cantidad * it.precio)/ factor
                def descuentoCalculado = (importe * descuento)/100.00
                descuentoCalculado = MonedaUtils.round (descuentoCalculado , 2)
                if(descuentoCalculado != it.descuentoImporte) {
                    log.debug('Error Descuento en partidas: {} calculado: {}', it.descuentoImporte, descuentoCalculado)
                    it.descuentoImporte = descuentoCalculado
                    it.total = it.subtotal + it.impuesto
                    it.descuentoImporte = it.importe - it.subtotal
                }
            }
            it.descuentoOriginal = it.descuentoOriginal > 0 ? venta.descuentoOriginal: it.descuentoOriginal
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
    def mandarFacturar(String ventaId, String usuario) {

        Venta venta = Venta.get(ventaId)
        log.debug('Mandando facturar venta......'+ venta.getFolio())
        
        venta.facturarUsuario = usuario
        venta.facturar = new Date()
        // venta.save()
        mandarFacturarCallCenter(venta)
        return venta
    }

    def mandarFacturarCallCenter(Venta venta) {
        if(venta.callcenter &&  venta.sw2 == null) 
            return
        log.info('Actualizando estatus de facturable en CallCenter (Firebase)')
        
        // 1. - Pedido
        lxPedidoService.updatePedido(venta.sw2, ['status': 'POR_FACTURAR', 'atiende': venta.facturarUsuario])
        
        // 2. - PedidoLog
        Map logChanges = [status: 'POR_FACTURAR', atiende: venta.facturarUsuario, facturable: venta.facturar, atendido: new Date()]
        lxPedidoService.updateLog(venta.sw2, logChanges)
    }

    def registrarPuesto(Venta venta, String usuario = null) {
        venta.puesto = new Date()
        venta.save flush: true
        registrarPuestoCallCenter(venta, usuario)
        return venta
    }
    def quitarPuesto(Venta venta) {
        venta.puesto = null
        venta.save flush: true
        registrarPuestoCallCenter(venta)
        return venta
    }

    def registrarPuestoCallCenter(Venta venta, String usuario) {
        if(venta.callcenter &&  venta.sw2 == null) 
            return
        log.info('Registrando PUESTO en Firebase')
        def puesto = venta.puesto
        Map changes = [puesto: null]
        if(puesto) {
            changes = [puesto: [fecha: puesto, usuario: usuario]]
        }
        // 1. - Pedido
        lxPedidoService.updatePedido(venta.sw2, changes)
        // 2. - PedidoLog
        lxPedidoService.updateLog(venta.sw2, changes)
    }


    @Publisher
    def facturar(Venta pedido) {
        log.debug("Facturando  ${pedido.statusInfo()}")
        assert pedido.cuentaPorCobrar == null, "Pedido${pedido.getFolio()} ya facturado : ${pedido.statusInfo()}"
        pedido = generarCuentaPorCobrar(pedido)
        return pedido
    }

    @Publisher('facturar')
    def generarCuentaPorCobrar(Venta pedido) {
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.sucursal = pedido.sucursal
        cxc.cliente = pedido.cliente
        cxc.tipoDocumento = 'VENTA'
        cxc.importe = pedido.importe
        cxc.descuentoImporte = pedido.descuentoImporte
        cxc.subtotal = pedido.subtotal
        cxc.impuesto = pedido.impuesto
        cxc.total  = pedido.total
        cxc.formaDePago = pedido.formaDePago
        cxc.moneda = pedido.moneda
        cxc.tipoDeCambio = pedido.tipoDeCambio
        cxc.comentario = pedido.comentario
        cxc.tipo = pedido.cod ? 'COD': pedido.tipo
        cxc.documento = Folio.nextFolio('FACTURAS',cxc.tipo)
        cxc.fecha = new Date()
        if(cxc.tipo == 'CRE') {
            Date vto = cxc.fecha + cxc.cliente.credito.plazo.intValue()
            cxc.vencimiento = vto
        }
        cxc.createUser = pedido.createUser
        cxc.updateUser = pedido.updateUser
        cxc.chequePostFechado = pedido.chequePostFechado
        cxc.comentario = pedido.comentario
        pedido.cuentaPorCobrar = cxc
        cxc.save failOnError: true
        log.debug('Cuenta por cobrar generada: {}', cxc)
        pedido.cuentaPorCobrar = cxc
        pedido.save flush: true
        inventarioService.afectarInventariosPorFacturar(pedido);
        
        // Actualizar Firebase Pedido y PedidoLog
        if(pedido.callcenter) {
            notificarFacturacionEnFirebase(pedido)
        }
        return pedido
    }

    /**
    * Firebase notification
    *
    * Fase: FACTURADO
    */
    def notificarFacturacionEnFirebase(Venta venta ) {
        def cxc = venta.cuentaPorCobrar
        def serie = venta.tipo
        def folio = cxc.documento.toString()
        
        // Log Pedido
        def changes = [
            status: 'FACTURADO',
            facturacion: [
                serie: serie,
                folio: folio, 
                creado: cxc.dateCreated
            ]
        ]
        lxPedidoService.updatePedido(venta.sw2, changes)
        lxPedidoService.updateLog(venta.sw2, changes)
    }

    def generarCfdi_Bak(Venta venta){
        assert venta.cuentaPorCobrar, " La venta ${venta.documento} no se ha facturado"
        def comprobante = cfdiFacturaBuilder.build(venta)

        def cfdi = cfdiService.generarCfdi(comprobante, 'I', venta.ventaIne)
        venta.cuentaPorCobrar.cfdi = cfdi
        venta.save flush: true
        return cfdi
    }

    @Transactional
    def timbrar(Venta venta){
        log.debug("Timbrando  {}", venta.statusInfo())
        def cxc = venta.cuentaPorCobrar
        def cfdi = cxc.cfdi
        cfdi.receptorRfc = cxc.cliente.rfc
        cfdi = cfdiTimbradoService.timbrar(cfdi)
        cxc.uuid = cfdi.uuid
        cxc.save flush:true

        // Notificar Firebase de facturacion
        if(venta.callcenter || venta.tipo) {
            notificarTimbradoEnFirebase(venta, cfdi)
            cfdiPdfService.pushToFireStorage(cfdi)
        }

        if(cxc.tipo == 'ANT') {
            anticipoSatService.generarAnticipo(cxc)
        }

        return cfdi
    }

    /**
    * Firebase notification
    *
    * Fase: FACTURADO_TIMBRADO
    */
    def notificarTimbradoEnFirebase(Venta venta, Cfdi cfdi) {
        if(venta.callcenter && venta.sw2) { 
            def changes = [
                status: 'FACTURADO_TIMBRADO',
                facturacion: [
                    serie: cfdi.serie,
                    folio: cfdi.folio, 
                    creado: cfdi.dateCreated,
                    cfdi: [id: cfdi.id, uuid: cfdi.uuid]
                ],
                // timbrado: cfdi.timbre.fechaTimbrado
            ]
            lxPedidoService.updatePedido(venta.sw2, changes)
            lxPedidoService.updateLog(venta.sw2, changes)
        }
    }

    @Transactional
    def generarCfdi(Venta venta){
        def cxc = venta.cuentaPorCobrar
        def comprobante = cfdiFacturaBuilder.build(venta)
        def cfdi = cfdiService.generarCfdi(comprobante, 'I', venta.ventaIne)
        cxc.cfdi = cfdi
        cxc.save(flush: true)
        return cfdi
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

    /**
     * Cancela una Factura (Venta facturada) cancelando su CFDI
     *
     * @param venta
     * @return
     */
    @Publisher
    def cancelarFactura(Venta factura, String username, String motivo, boolean validarDia = true) {
        assert factura.cuentaPorCobrar, "El pedido ${factura.statusInfo()} no esta facturado "
        assert username, 'Debe registrar usiuario para la cancelacion'
        assert motivo, 'Debe registrar motivo de cancelacion'

        if(validarDia) {
            Date hoy = new Date()
            boolean mismoDia = DateUtils.isSameDay(hoy, factura.cuentaPorCobrar.fecha)
            if(!mismoDia){
                throw new RuntimeException("La Factura ${factura.statusInfo()} no es del dia  no se puede cancelar")
            }
        }


        log.debug('Cancelando factura {}', factura.statusInfo())
        CuentaPorCobrar cxc = factura.cuentaPorCobrar
        Cfdi cfdi = cxc.cfdi

        // 1o Desvincular la cuenta por cobrar y la venta
        factura.cuentaPorCobrar = null
        factura.facturar = null
        factura.impreso = null
        factura.save flush: true

        // 2o Eliminar la cuenta por cobrar sus aplicaciones y cancelar su CFDI
        eliminarAplicaciones(cxc)
        cancelarCuentaPorCobrar(cxc, username, motivo)
        inventarioService.afectarInventariosPorCancelacionDeFacturar(factura)

        // 3o Cancelar el CFDI
        if( cfdi && cfdi.uuid) {
            cfdi.status = 'CANCELACION_PENDIENTE'
            cfdi.save flush:true
        }
        if (factura.callcenter ) {
            notificarCancelacionEnFirebase(factura)
        }
        return factura
    }

    /**
     * Elimina la CuentaPorCobrar
     *
     * @param cxc
     * @return
     */
    // @Publisher
    def cancelarCuentaPorCobrar(CuentaPorCobrar cxc, String usuario, String motivo) {
        cxc.importe = 0.0
        cxc.impuesto = 0.0
        cxc.subtotal = 0.0
        cxc.descuentoImporte = 0.0
        cxc.total = 0.0
        cxc.comentario = 'CANCELADA'
        cxc.cancelada = new Date()
        cxc.cancelacionUsuario = usuario
        cxc.cancelacionMotivo = motivo
        cxc.save()
        // cxc.delete flush: true
    }

    /**
     * Elimina las aplicaciones de cobro de la cuenta por cobrar
     *
     * @param cxc
     * @return La cuenta por cobar sin aplicaciones sociadas
     */
    // @Publisher
    def eliminarAplicaciones(CuentaPorCobrar cxc) {
        def aplicaciones = AplicacionDeCobro.where{ cuentaPorCobrar == cxc}.list()
        log.debug('Eliminando {} aplicaciones a la factura {}', aplicaciones.size(), cxc.folio)
        aplicaciones.each { AplicacionDeCobro a ->
            Cobro cobro = a.cobro
            if(cobro.aplicaciones.size() == 1 ){
                if (cxc.formaDePago != 'TRANSFERENCIA' && !cxc.formaDePago.startsWith("DEPOSITO")) {
                    log.debug('Eliminando cobro {} {}', cobro.formaDePago, cobro)
                    cobro.delete flush:true;
                } else {
                    cobro.removeFromAplicaciones(a)
                    cobro.save()
                }
            } else {
                cobro.removeFromAplicaciones(a)
                cobro.save()
            }
        }
        return cxc
    }

    /**
    * Firebase notification
    *
    * Fase: FACTURADO_TIMBRADO
    */
    def notificarCancelacionEnFirebase(Venta venta) {
        if(venta.callcenter && venta.sw2) { 
            def changes = [
                status: 'FACTURADO_CANCELADO',
                facturacion: null,
                timbrado: null
            ]
            lxPedidoService.updatePedido(venta.sw2, changes)
            lxPedidoService.updateLog(venta.sw2, changes)
        }
    }


    def regresaraPendiente(Venta venta) {
        venta.facturar = null
        venta = venta.save(flush: true)
        def changes = [status: 'FACTURABLE']
        lxPedidoService.updatePedido(venta.sw2, changes)
        lxPedidoService.updateLog(venta.sw2, changes)
        return venta
    }

    void regresarCallcenter(Venta venta, def usuario) {
        venta.delete flush: true
        def changes = [status: 'COTIZACION', updateUser: usuario]
        lxPedidoService.updatePedido(venta.sw2, changes)
        lxPedidoService.updateLog(venta.sw2, changes)
    }



}
