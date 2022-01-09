/*
 * Copyright (c) 2018. Ruben Cancino Ramos Derechos de uso reservados
 */
package sx.core

import com.luxsoft.cfdix.v33.CfdiFacturaBuilder
import com.luxsoft.utils.MonedaUtils
import grails.events.EventPublisher
import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.lang3.time.DateUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiPdfService
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.cloud.LxPedidoService
import sx.cloud.papws.PedidosService
import sx.cxc.*
import sx.inventario.InventarioService

/**
 * @author: Ruben Cancino Ramos
 *
 */
@Transactional
@Slf4j
class VentaService implements  EventPublisher{

    CfdiService cfdiService

    CfdiFacturaBuilder cfdiFacturaBuilder

    CfdiTimbradoService cfdiTimbradoService

    SpringSecurityService springSecurityService

    InventarioService inventarioService

    LxPedidoService lxPedidoService
    PedidosService pedidosService

    CfdiPdfService cfdiPdfService

    AnticipoSatService anticipoSatService

    @Publisher
    def save(Venta venta) {
        if(venta.tipo == 'ANT'){
            return saveVentaDeAnticipo(venta)
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

    def saveVentaDeAnticipo(Venta venta) {
        log.debug('Generando venta de anticipo...')
        fixNombre(venta)
        fixVendedor(venta)

        def total = venta.total
        def importe = MonedaUtils.calcularImporteDelTotal(total, 6)
        def impuesto = MonedaUtils.calcularImpuesto(importe)
        impuesto = MonedaUtils.round(impuesto)
        importe = MonedaUtils.round(importe)
        log.info('Total: {} Importe: {} Impuesto: {}', total, importe, impuesto)
        assert total == (importe + impuesto)
        venta.importe = importe
        venta.subtotal = importe
        venta.impuesto = impuesto
        venta.total = importe + impuesto

        def det = venta.partidas.get(0)
        det.importe = importe
        det.subtotal = importe
        det.impuesto = impuesto
        det.total = venta.total

        if(venta.id == null){
            Folio folio=Folio.findOrCreateWhere(entidad: 'VENTAS', serie: 'ANTICIPOS')
            def res = folio.folio + 1
            folio.folio = res
            venta.documento = res
            folio.save()
        }
        
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
    Venta mandarFacturar(String ventaId, String usuario) {

        Venta venta = Venta.get(ventaId)
        log.debug('Mandando facturar venta......'+ venta.getFolio())
        
        venta.facturarUsuario = usuario
        venta.facturar = new Date()
        // venta.save()
        mandarFacturarCallCenter(venta)
        return venta
    }

    /**
     * Notificar al Callcenter del
     * @param venta
     * @return
     */
    def mandarFacturarCallCenter(Venta venta) {
        if(venta.callcenter) {
            if(venta.callcenterVersion == 2) {
                this.pedidosService.mandarFacturar(venta)
            } else {
                lxPedidoService.updatePedido(venta.sw2, ['status': 'POR_FACTURAR', 'atiende': venta.facturarUsuario])
                Map logChanges = [status: 'POR_FACTURAR', atiende: venta.facturarUsuario, facturable: venta.facturar, atendido: new Date()]
                lxPedidoService.updateLog(venta.sw2, logChanges)
            }
        }
    }

    def registrarPuesto(Venta venta, String usuario = null) {
        venta.puesto = new Date()
        venta.save flush: true
        registrarPuestoCallCenter(venta, usuario)
        return venta
    }
    def quitarPuesto(Venta venta,  String usuario = null) {
        venta.puesto = null
        venta.save flush: true
        registrarPuestoCallCenter(venta,usuario)
        return venta
    }

    /**
     *  Registra el puesto en el Callcenter
     *
     * @param venta
     * @param usuario
     * @return
     */
    def registrarPuestoCallCenter(Venta venta, String usuario) {
        try {
            // if(venta.callcenter &&  venta.sw2 == null)  // Migracion a callcenter2
                // return
            if(venta.callcenterVersion == 2) {
                this.pedidosService.registrarPuesto(venta, usuario)
            } else {
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

        } catch (Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error registrando puesto en firebase: ' + message, ex)
        }
    }

    def validarSaldoCre(Venta pedido){
        if(pedido.tipo == 'CRE'){
            def clienteCredito = ClienteCredito.findByCliente(pedido.cliente)
            def saldo =  clienteCredito.lineaDeCredito - clienteCredito.saldo
            println pedido.total
            if(pedido.total > saldo){
                return [message:"No se Puede Facturar, el importe del pedido (${pedido.total}) es mayor al saldo del cliente (${saldo})".toString(), facturar: false]
            }else{
                return [message:"Facturable".toString(), facturar: true]
            }
        }
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
        log.debug('Cuenta por cobrar generada: {}', cxc.id)
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
        try {
            if(venta.callcenterVersion == 2) {
                pedidosService.notificarFacturacion(venta)
            } else {
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
        } catch(Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error notificando en firebase: ' + message, ex)
        }
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
        if(venta.callcenter ) {
            notificarTimbradoEnFirebase(venta, cfdi)
            cfdiPdfService.pushToFireStorage(cfdi)
            /*
            try {

            } catch(Exception ex) {
                String message = ExceptionUtils.getRootCauseMessage(ex)
                log.error('Error subiendo factura  a firestore: ' + message)
            }
            */
        }

        if (venta.tipo == 'CRE') {
            cfdiPdfService.pushToFireStorage(cfdi)
        }

        if(cxc.tipo == 'ANT') {
            def anticipo = anticipoSatService.generarAnticipo(cxc)
            anticipo.refresh()
            anticipoSatService.updateFirebase(anticipo)
            cfdiPdfService.pushToFireStorage(cfdi)
        }

        if(cxc.anticipo) {
            log.debug('Actualizando anticipo')
            def anticipoDet = AnticipoSatDet.where{cxc == cxc.id}.find()
            if(anticipoDet) {
                anticipoDet.uuid = cfdi.uuid
                anticipoDet = anticipoDet.save flush: true
                anticipoSatService.updateFirebase(anticipoDet.anticipo)
            }
        }
        return cfdi
    }

    /**
    * Firebase notification
    *
    * Fase: FACTURADO_TIMBRADO
    */
    def notificarTimbradoEnFirebase(Venta venta, Cfdi cfdi) {
        try {
            if(venta.callcenterVersion == 2) {
                log.debug('Notificando Callcenter 2')
                pedidosService.notificarTimbrado(venta, cfdi)
                log.debug('PAPWS NOTIFICADO OK...')
            } else {
                log.debug('Notificando Callcenter 1')
                def changes = [
                  status: 'FACTURADO_TIMBRADO',
                  facturacion: [
                    serie: cfdi.serie,
                    folio: cfdi.folio,
                    creado: cfdi.dateCreated,
                    cfdi: [id: cfdi.id, uuid: cfdi.uuid]
                  ],
                ]
                lxPedidoService.updatePedido(venta.sw2, changes)
                lxPedidoService.updateLog(venta.sw2, changes)
            }
        } catch (Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error notificando timbrado de factura en firebase: ' + message, ex)
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

        log.debug('Cancelando factura {}', factura.statusInfo())
        CuentaPorCobrar cxc = factura.cuentaPorCobrar



        if(validarDia) {
            Date hoy = new Date()
            boolean mismoDia = DateUtils.isSameDay(hoy, factura.cuentaPorCobrar.fecha)
            if(!mismoDia){
                throw new RuntimeException("La Factura ${factura.statusInfo()} no es del dia  no se puede cancelar")
            }
        }

        if(cxc.anticipo) {
            log.debug('Cancelando aplicacion de anticipo')
            def anticipoDet = AnticipoSatDet.where{cxc == cxc.id}.find()
            if(anticipoDet) {
                def anticipo = anticipoDet.anticipo
                if(anticipoDet.egresoUuid) {
                    throw new RuntimeException("""
                        Esta venta facturada no se puede cancelar al tener un anticipo aplicado con 
                        EGRESO TIMBRADO:${anticipoDet.egresoUuid} 
                        """)
                }
                anticipoDet.delete flush: true
                anticipoSatService.updateFirebase(anticipo)
            }
        }

        if(cxc.tipo == 'ANT') {
            AnticipoSat anticipo = AnticipoSat.where{cxc == cxc.id}.find()
            // def aplicaciones = AnticipoSatDet.findAll("from AnticipoSatDet a where a.anticipo.cxc = ?", cxc.id)
            if(anticipo.aplicaciones) {
                throw new RuntimeException("""
                        Esta facturada de anticipo no se puede cancelar por que ya se a usado para pagar 
                        ${anticipo.aplicaciones.size()} venta(s)
                        """)
            } 
        } 
        

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
        // Si es anticipo eliminar el registro de AnticipoSat local y en firebase
        if(cxc.tipo == 'ANT') {
            AnticipoSat anticipo = AnticipoSat.where{cxc == cxc.id}.find()
            anticipo.delete flush: true
            anticipoSatService.deleteFromFirebase(anticipo)
        }

        // Si se aplico un AnticipoSatDet entonces eliminarlo local y en firebase
        if(cxc.anticipo) {
            log.debug('Cancelando aplicacion de anticipo')
            def anticipoDet = AnticipoSatDet.where{cxc == cxc.id}.find()
            if(anticipoDet) {
                def anticipo = anticipoDet.anticipo
                anticipoDet.delete flush: true
                anticipoSatService.updateFirebase(anticipo)
            }
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
        if(venta.callcenter) { 
            if(venta.callcenterVersion == 2) {
                pedidosService.cancelarFactura(venta)
            } else {
                def changes = [
                    status: 'FACTURADO_CANCELADO',
                    facturacion: null,
                    timbrado: null
                ]
                lxPedidoService.updatePedido(venta.sw2, changes)
                lxPedidoService.updateLog(venta.sw2, changes)
            }
            
        }
    }


    def regresaraPendiente(Venta venta) {
        venta.facturar = null
        venta = venta.save(flush: true)
        if(venta.callcenter) {
            if(venta.callcenterVersion == 2) {
                pedidosService.regresaraPendiente(venta);
            } else {
                def changes = [status: 'FACTURABLE']
                lxPedidoService.updatePedido(venta.sw2, changes)
                lxPedidoService.updateLog(venta.sw2, changes)
            }
        }
        return venta
    }

    void regresarCallcenter(Venta venta, def usuario) {
        venta.delete flush: true
        if(venta.callcenter) {
            if(venta.callcenterVersion == 2) {
                this.pedidosService.regresaraCallcenter(venta, usuario)
            } else {
                def changes = [status: 'COTIZACION', updateUser: usuario]
                lxPedidoService.updatePedido(venta.sw2, changes)
                lxPedidoService.updateLog(venta.sw2, changes)
            }
        }
        
    }



}
