package sx.tesoreria

import com.luxsoft.utils.MonedaUtils
import grails.gorm.transactions.Transactional
import sx.core.Empresa
import sx.core.Folio
import sx.core.Sucursal
import sx.cxc.Cobro
import sx.cxc.CobroTarjeta


class CorteDeTarjetaService {

    def generar(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {
        generarCorteVisaMastercard(fecha, sucursal, cobros)
        generarCorteAmex(fecha, sucursal, cobros)
    }

    @Transactional
    def generarCorteVisaMastercard(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {
        List visaMastercard = cobros.findAll { it.visaMaster}
        if (!visaMastercard)
            return
        BigDecimal total = visaMastercard.sum(0.0, { it.cobro.importe})
        BigDecimal debito = visaMastercard.sum(0.0, {it.debitoCredito ? it.cobro.importe : 0.0})
        BigDecimal debitoComision = MonedaUtils.round(debito * (1.46 / 100))
        BigDecimal debitoComisionIva = MonedaUtils.round(debitoComision * MonedaUtils.IVA)

        BigDecimal credito = visaMastercard.sum 0.0, { !it.debitoCredito ? it.cobro.importe : 0.0}
        BigDecimal creditoComision = MonedaUtils.round(credito * (2.36 / 100))
        BigDecimal creditoComisionIva = MonedaUtils.round(creditoComision * MonedaUtils.IVA)


        CorteDeTarjeta corte = new CorteDeTarjeta()
        corte.sucursal = sucursal
        corte.visaMaster = true
        corte.folio = Folio.nextFolio('CORTE_TARJETA', sucursal.nombre)
        corte.corte = fecha
        corte.total = total
        corte.cuentaDeBanco = CuentaDeBanco.where{numero == 1858193}.find()
        corte.comentario = 'CORTE AUTOMATICO'

        // INGRESO
        CorteDeTarjetaAplicacion ingreso = new CorteDeTarjetaAplicacion()
        ingreso.importe = total
        ingreso.visaMaster = true
        ingreso.debitoCredito = false
        ingreso.tipo = TipoDeAplicacion.VISAMASTER_INGRESO
        corte.addToAplicaciones(ingreso)

        // DEBITO
        CorteDeTarjetaAplicacion comisionDebito = new CorteDeTarjetaAplicacion()
        comisionDebito.importe = debitoComision
        comisionDebito.visaMaster = true
        comisionDebito.debitoCredito = true
        comisionDebito.tipo = TipoDeAplicacion.DEBITO_COMISION
        corte.addToAplicaciones(comisionDebito)

        CorteDeTarjetaAplicacion comisionDebitoIva = new CorteDeTarjetaAplicacion()
        comisionDebitoIva.importe = debitoComisionIva
        comisionDebitoIva.visaMaster = true
        comisionDebitoIva.debitoCredito = true
        comisionDebitoIva.tipo = TipoDeAplicacion.DEBITO_COMISION_IVA
        corte.addToAplicaciones(comisionDebitoIva)

        // CREDITO
        CorteDeTarjetaAplicacion comisionCredito = new CorteDeTarjetaAplicacion()
        comisionCredito.importe = creditoComision
        comisionCredito.tipo = TipoDeAplicacion.CREDITO_COMISION
        comisionCredito.visaMaster = true
        comisionCredito.debitoCredito = false
        corte.addToAplicaciones(comisionCredito)

        CorteDeTarjetaAplicacion comisionCreditoIva = new CorteDeTarjetaAplicacion()
        comisionCreditoIva.tipo = TipoDeAplicacion.CREDITO_COMISION_IVA
        comisionCreditoIva.importe = creditoComisionIva
        comisionCreditoIva.visaMaster = true
        comisionCreditoIva.debitoCredito = false
        corte.addToAplicaciones(comisionCreditoIva)

        corte.save()
        visaMastercard.each { CobroTarjeta it ->
            CobroTarjeta cobroTarjeta = CobroTarjeta.get(it.id)
            cobroTarjeta.corteDeTarjeta = corte
            cobroTarjeta.corte = corte.id
            cobroTarjeta.save()
        }

    }

    @Transactional
    def generarCorteAmex(Date fecha, Sucursal sucursal, List<CobroTarjeta> cobros) {

        List<CobroTarjeta> amex = cobros.findAll{!it.visaMaster}

        if(amex) {
            BigDecimal amexIngreso = amex.sum 0.0, {it.cobro.importe}
            BigDecimal amexComision = MonedaUtils.round( amexIngreso * (3.35 / 100), 2)
            BigDecimal amexComisionIva = MonedaUtils.round(amexComision * MonedaUtils.IVA, 2)

            CorteDeTarjeta corte = new CorteDeTarjeta()
            corte.sucursal = sucursal
            corte.visaMaster = false
            corte.folio = Folio.nextFolio('CORTE_TARJETA', sucursal.nombre)
            corte.corte = fecha
            corte.total = amexIngreso
            corte.cuentaDeBanco = CuentaDeBanco.where{numero == 1858193}.find()
            corte.comentario = 'CORTE AUTOMATICO AMEX'

            // INGRESO
            CorteDeTarjetaAplicacion ingreso = new CorteDeTarjetaAplicacion()
            ingreso.importe = amexIngreso
            ingreso.visaMaster = false
            ingreso.debitoCredito = false
            ingreso.tipo = TipoDeAplicacion.AMEX_INGRESO
            corte.addToAplicaciones(ingreso)

            // COMISION
            CorteDeTarjetaAplicacion comision = new CorteDeTarjetaAplicacion()
            comision.importe = amexComision
            comision.visaMaster = false
            comision.debitoCredito = false
            comision.tipo = TipoDeAplicacion.AMEX_COMISION
            corte.addToAplicaciones(comision)

            CorteDeTarjetaAplicacion comisionIva = new CorteDeTarjetaAplicacion()
            comisionIva.importe = amexComisionIva
            comisionIva.visaMaster = true
            comisionIva.debitoCredito = true
            comisionIva.tipo = TipoDeAplicacion.AMEX_COMISION_IVA
            corte.addToAplicaciones(comisionIva)

            corte.save()

            amex.each {
                log.debug('Actualizando cobro: {}', it.id)
                CobroTarjeta cobroTarjeta = CobroTarjeta.get(it.id)
                cobroTarjeta.corte = corte.id
                cobroTarjeta.save()
            }

        }
    }

    @Transactional
    def ajustarCobro(Cobro cobro, CobroTarjeta tarjet){

    }

    @Transactional
    def eliminarCorte(CorteDeTarjeta corte) {
        CobroTarjeta.executeUpdate(
                "update from CobroTarjeta set corte = null where corte = ?",
                [corte.id])
        corte.delete flush: true
    }

    @Transactional
    def aplicar(CorteDeTarjeta corte){
        log.debug('Aplicando corte: {}',corte.id)
        Empresa empresa = Empresa.first()
        corte.aplicaciones.each { CorteDeTarjetaAplicacion aplicacion ->
            String comentario = ''
            BigDecimal importe = aplicacion.importe
            switch (aplicacion.tipo) {
                case TipoDeAplicacion.AMEX_INGRESO:
                    comentario = "Corte por tarjeta Amex ${corte.corte.format('dd/MM/yyyy')} ${corte.sucursal.nombre}"
                    break
                case TipoDeAplicacion.VISAMASTER_INGRESO:
                    comentario = "Corte por tarjeta Visa/Mastercard ${corte.corte.format('dd/MM/yyyy')} ${corte.sucursal.nombre}"
                    break
                case TipoDeAplicacion.AMEX_COMISION:
                    comentario = "Comisión por tarjeta AMEX "
                    importe = importe * -1
                    break
                case TipoDeAplicacion.CREDITO_COMISION:
                    comentario = "Comisión por tarjeta CREDITO "
                    importe = importe * -1
                    break
                case TipoDeAplicacion.DEBITO_COMISION:
                    comentario = 'Comision por tarjeta de DEBITO'
                    importe = importe * -1
                    break
                case TipoDeAplicacion.AMEX_COMISION_IVA:
                    comentario = "IVA Comision tarjeta AMEX}"
                    importe = importe * -1
                    break
                case TipoDeAplicacion.DEBITO_COMISION_IVA:
                    comentario = "IVA Comision tarjeta DEBITO}"
                    importe = importe * -1
                    break
                case TipoDeAplicacion.CREDITO_COMISION_IVA:
                    comentario = "IVA Comision tarjeta CREDITO}"
                    importe = importe * -1
                    break

            }
            MovimientoDeCuenta mov = new MovimientoDeCuenta()
            mov.sucursal = corte.sucursal.nombre
            mov.referencia = "${aplicacion.tipo}"
            mov.tipo = aplicacion.visaMaster ? 'VISA-MASTERCARD' : 'AMEX'
            mov.fecha = corte.corte
            mov.formaDePago = 'TARJETA'
            mov.comentario = comentario
            mov.cuenta = corte.cuentaDeBanco
            mov.afavor = empresa.nombre
            mov.importe = importe
            mov.moneda = mov.cuenta.moneda
            mov.concepto = 'VENTAS'
            mov.conceptoReporte = "Deposito suc: ${mov.sucursal}"
            mov.save failOnError: true, flush: true
            aplicacion.ingreso = mov;

        }
        corte.save flush: true

    }

    @Transactional
    def cancelarAplicacion(CorteDeTarjeta corte){
        corte.aplicaciones.each {
            def ingreso = it.ingreso
            it.ingreso = null
            it.save flush: true
            ingreso.delete flush: true
        }
        return corte
    }

    public actualizarComisiones(CobroTarjeta tarjeta) {
        if(tarjeta.visaMaster){
            if(tarjeta.debitoCredito) {
                tarjeta.comision = 1.46
            } else {
                tarjeta.comision = 2.36
            }
        } else {
            tarjeta.comision = 3.75
        }
    }
}
