package sx.cxc

import grails.gorm.transactions.Transactional

@Transactional
class CobroService {

    def ventaService

    def generarCobroDeContado(CuentaPorCobrar cxc, List<Cobro> cobros) {
        def saldo = cxc.saldo
        cobros.each { cobro ->
            if (cobro.importe > 0) {
                def disponible = cobro.disponible
                def importe = disponible < saldo ? disponible : saldo
                def aplicacion = new AplicacionDeCobro()
                aplicacion.cuentaPorCobrar = cxc
                aplicacion.fecha = new Date()
                aplicacion.importe = importe
                aplicacion.formaDePago = cobro.formaDePago
                cobro.addToAplicaciones(aplicacion)

                // Saldos <= 1 peso
                def saldoNuevo = saldo - importe
                if (saldoNuevo > 0 && saldoNuevo <= 1.0) {
                    generarCobroDeDiferencia(cxc, saldoNuevo, cobro)
                }
                if(!cobro.primeraAplicacion) {
                    cobro.primeraAplicacion = aplicacion.fecha
                }
                disponible = disponible - aplicacion.importe

                if(disponible < 10 && disponible > 0.01) {
                    cobro.diferencia = disponible
                    cobro.diferenciaFecha = new Date()
                }

                setComisiones(cobro)
                cobro.tipo = cxc.tipo
                if(cobro.cheque) {
                    cobro.referencia = cobro.cheque.numero.toString()
                }
                if(cobro.tarjeta) {
                    cobro.referencia = cobro.tarjeta.validacion
                }
                cobro.save()  //failOnError: true, flush: true
                saldo = saldo - importe
            }
        }
        return cxc
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

    private generarCobroDeDiferencia( CuentaPorCobrar cxc, BigDecimal saldoNuevo, Cobro cobro) {
        assert saldoNuevo <= 1.0, 'No es Diferencia'
        Cobro cobroDif = new Cobro()
        cobroDif.cliente = cobro.cliente
        cobroDif.fecha = new Date()
        cobroDif.sucursal = cxc.sucursal
        cobroDif.importe = saldoNuevo
        cobroDif.comentario = 'COBRO AUTOMATICO'
        cobroDif.formaDePago = 'PAGO_DIF'
        cobroDif.tipo = cxc.tipo
        cobroDif.updateUser = cobro.updateUser
        cobroDif.createUser = cobro.updateUser
        cobroDif.primeraAplicacion = new Date()

        def aplicacion = new AplicacionDeCobro()
        aplicacion.cuentaPorCobrar = cxc
        aplicacion.fecha = new Date()
        aplicacion.importe = saldoNuevo
        aplicacion.formaDePago = cobroDif.formaDePago
        cobroDif.addToAplicaciones(aplicacion)
        cobroDif.save()


    }


}
