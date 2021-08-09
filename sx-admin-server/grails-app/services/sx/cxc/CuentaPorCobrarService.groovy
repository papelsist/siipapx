package sx.cxc

import grails.gorm.transactions.Transactional
import sx.core.AppConfig

@Transactional
class CuentaPorCobrarService {

    CuentaPorCobrar saldar(CuentaPorCobrar cxc) {
        if (cxc.saldo > 0.0 && cxc.saldo <= 100.00) {
            Date fecha = new Date()
            Cobro cobro = new Cobro()
            cobro.sucursal = AppConfig.first().sucursal
            cobro.fecha = fecha
            cobro.tipo = cxc.tipo
            cobro.formaDePago = 'PAGO_DIF'
            cobro.importe = cxc.saldo
            cobro.fechaDeAplicacion = fecha
            cobro.referencia = cxc.folio
            cobro.cliente = cxc.cliente
            AplicacionDeCobro aplicacionDeCobro = new AplicacionDeCobro()
            aplicacionDeCobro.importe = cxc.saldo
            aplicacionDeCobro.formaDePago = 'PAGO_DIF'
            aplicacionDeCobro.fecha = fecha
            aplicacionDeCobro.cuentaPorCobrar = cxc
            cobro.addToAplicaciones(aplicacionDeCobro)
            cobro.save failOnError: true, flush: true

        }
        return cxc
    }
}

