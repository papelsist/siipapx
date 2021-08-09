package sx.cxc

import com.luxsoft.cfdix.v33.ReciboDePagoBuilder
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.Comprobante
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.core.Cliente
import sx.core.Folio

@Transactional
class CobroService {

    ReciboDePagoBuilder reciboDePagoBuilder

    CfdiService cfdiService

    CfdiTimbradoService cfdiTimbradoService

    Cobro save(Cobro cobro) {
        if(cobro.cheque) {
            cobro.referencia = cobro.cheque.numero
        }
        if(cobro.tarjeta) {
            cobro.referencia = cobro.tarjeta.validacion.toString()
        }
        setComisiones(cobro)
        if(cobro.id == null) {
            cobro.fecha = new Date()
        }
        cobro.save flush:true

    }


    def generarCobroDeContado(CuentaPorCobrar cxc, List<Cobro> cobros) {
        def saldo = cxc.saldo
        cobros.each { cobro ->
            def disponible = cobro.disponible
            def importe = disponible < saldo ? disponible : saldo
            def aplicacion = new AplicacionDeCobro()
            aplicacion.cuentaPorCobrar = cxc
            aplicacion.fecha = new Date()
            aplicacion.importe = importe
            cobro.addToAplicaciones(aplicacion)
            disponible = disponible - aplicacion.importe

            if(disponible < 10 && disponible > 0.01) {
                cobro.diferencia = disponible
                cobro.diferenciaFecha = new Date()
            }
            setComisiones(cobro)
            cobro.save failOnError: true, flush: true
            saldo = saldo - importe
        }
        return cxc
    }

    def registrarAplicacion(Cobro cobro, List<CuentaPorCobrar> pendientes){
        def fecha = new Date()
        def disponible = cobro.disponible
        if (disponible <= 0)
            return cobro
        pendientes.each { cxc ->
            def saldo = cxc.saldo
            if (disponible > 0) {
                def importe = saldo <= disponible ? saldo : disponible
                AplicacionDeCobro aplicacion = new AplicacionDeCobro()
                aplicacion.importe = importe
                aplicacion.formaDePago = cobro.formaDePago
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = fecha

                aplicacion.moneda = cxc.moneda.getCurrencyCode()
                aplicacion.tipoDeCambio = cxc.tipoDeCambio

                cobro.addToAplicaciones(aplicacion)
                if(cobro.primeraAplicacion == null)
                    cobro.primeraAplicacion = fecha

                disponible = disponible - importe
            }
        }
        cobro.save flush: true
        return cobro
    }

    Cobro eliminarAplicacion(AplicacionDeCobro aplicacionDeCobro) {
        Cobro cobro = aplicacionDeCobro.cobro
        if(cobro.cfdi) {
            throw new RuntimeException("Cobro con recibo de pago (CFDI)  ${cobro.cfdi.uuid} ya generado NO SE PUEDENDEN MODIFICAR APLICACIONES")
        }
        cobro.removeFromAplicaciones(aplicacionDeCobro)
        if(!cobro.aplicaciones) {
            cobro.primeraAplicacion = null
        }
        cobro.save flush: true
        return cobro
    }

    def registrarAplicacion2(Cobro cobro){

        def disponible = cobro.disponible
        if (disponible <= 0)
            return cobro
        def pendientes = cobro.pendientesDeAplicar
        def fecha = cobro.fechaDeAplicacion ?: new Date()
        pendientes.each { cxc ->
            def saldo = cxc.saldo
            if (disponible > 0) {
                def importe = saldo <= disponible ? saldo : disponible
                AplicacionDeCobro aplicacion = new AplicacionDeCobro()
                aplicacion.importe = importe
                aplicacion.formaDePago = cobro.formaDePago
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = fecha
                cobro.addToAplicaciones(aplicacion)
                if(cobro.primeraAplicacion == null)
                    cobro.primeraAplicacion = fecha
                disponible = disponible - importe
            }
        }
        cobro.save()
        return cobro
    }


    def saldar(Cobro cobro){
        if(cobro.disponible <= 100.00 && cobro.disponible > 0.00) {
            cobro.diferencia = cobro.disponible;
            cobro.diferenciaFecha = new Date()
            cobro.save flush: true
        }
        return cobro
    }

    private setComisiones(Cobro cobro) {
        if (cobro.tarjeta) {
            if(cobro.tarjeta.debitoCredito) {
                cobro.tarjeta.comision = 1.46
            } else if (cobro.tarjeta.visaMaster) {
                cobro.tarjeta.comision = 2.36
            } else {
                cobro.tarjeta.comision = 3.80
            }
        }
    }


    def generarCfdi(Cobro cobro) {
        validarParaTimbrado(cobro)
        log.debug(' Generando recibo electronico de pago para cobro: {}', cobro.id)
        Comprobante comprobante = this.reciboDePagoBuilder.build(cobro)
        
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'P', 'COBROS')
        log.debug('CFDI generado {}', cfdi.id)
        cobro.cfdi = cfdi
        cobro.save flush: true
        
        return cobro
    }

    def timbrar(Cobro cobro){
        if(!cobro.cfdi) {
            cobro = generarCfdi(cobro)
        }
        cfdiTimbradoService.timbrar(cobro.cfdi)
        cobro.refresh()
        return cobro
    }

    def validarParaTimbrado(Cobro cobro) {
        if(!cobro.aplicaciones) {
            throw new RuntimeException("El cobro debe tener al menos una a plicacion")
        }
        def sinCfdi = cobro.aplicaciones.find {AplicacionDeCobro det -> det.cuentaPorCobrar.cfdi == null}
        assert sinCfdi == null , 'El cobro no debe tener aplicaciones a facturas sin timbrar'
        List<AplicacionDeCobro> contado = cobro.aplicaciones.find{AplicacionDeCobro det -> det.cuentaPorCobrar.tipo == 'CON' }
        assert contado == null, 'El cobro no debe tener aplicaciones de contado'
    }

    def envioDeRecibo(Cobro cobro, String targetEmail) {
        if(cobro.cfdi == null) {
            throw new RuntimeException("Cobro ${cobro.id} no tiene recibo (CFDI) generado")
        }
        if(!targetEmail) {
            Cliente cliente = cobro.cliente
            if(!cliente.cfdiValidado) {
                throw new RuntimeException("El Cliente ${cliente.nombre} no tiene su Email validado, no se puede mandar el correo")
            }
            targetEmail = cliente.getCfdiMail()
        }
        Cfdi cfdi = cobro.cfdi
        String message = """Apreciable cliente por este medio le hacemos llegar un comprobante fiscal digital (CFDI) . Este correo se envía de manera autmática favor de no responder a la dirección del mismo. Cualquier duda o aclaración
                la puede dirigir a: servicioaclientes@papelsa.com.mx
            """
        def xml = cfdi.getUrl().getBytes()
        def pdf = generarImpresionV33(cfdi).toByteArray()
        sendMail {
            multipart false
            to targetEmail
            from 'facturacion@papelsa.mobi'
            subject "CFDI ${cfdi.serie}-${cfdi.folio}"
            text message
            attach("${cfdi.serie}-${cfdi.folio}.xml", 'text/xml', xml)
            attach("${cfdi.serie}-${cfdi.folio}.pdf", 'application/pdf', pdf)
        }
        cfdi.enviado = new Date()
        cfdi.email = targetEmail
        cfdi.save flush: true
        log.debug('CFDI: {} enviado a: {}', cfdi.uuid, targetEmail)
    }


}
