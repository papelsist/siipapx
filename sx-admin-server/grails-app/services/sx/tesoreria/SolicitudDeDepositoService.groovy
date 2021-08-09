package sx.tesoreria

import com.luxsoft.utils.Periodo
import grails.gorm.transactions.Transactional
import sx.core.Empresa
import sx.cxc.Cobro
import sx.cxc.CobroDeposito
import sx.cxc.CobroTransferencia
import sx.cxc.SolicitudDeDeposito

@Transactional
class SolicitudDeDepositoService {

    def autorizar(SolicitudDeDeposito solicitud) {
        assert solicitud.cobro == null, 'Solicitud ya autorizada'
        log.debug('Autorizando solicitud: {}',solicitud.folio)
        Cobro cobro = new Cobro()
        cobro.sucursal = solicitud.sucursal
        cobro.cliente = solicitud.cliente
        cobro.fecha = new Date()
        cobro.tipo = solicitud.tipo
        if(solicitud.tipo == 'NORMAL') {
            cobro.tipo = 'CON'
        }
        if (solicitud.transferencia > 0) {
            cobro.formaDePago = 'TRANSFERENCIA'
        } else {
            if (solicitud.cheque > solicitud.efectivo) {
                cobro.formaDePago = 'DEPOSITO_CHEQUE'
            } else {
                cobro.formaDePago = 'DEPOSITO_EFECTIVO'
            }
        }
        cobro.referencia = solicitud.referencia
        cobro.importe = solicitud.total
        cobro.save flush: true
        solicitud.cobro = cobro
        solicitud.save flush: true

        if(solicitud.transferencia > 0.0 ){
            CobroTransferencia transferencia = new CobroTransferencia()
            transferencia.cuentaDestino = solicitud.cuenta
            transferencia.bancoOrigen = solicitud.banco
            transferencia.fechaDeposito = solicitud.fechaDeposito
            transferencia.folio = solicitud.folio
            transferencia.cobro = cobro
            transferencia.save flush: true
            if(['CRE','CHE','JUR'].contains(cobro.tipo)) {
                registrarIngreso(cobro)
            }

        } else {
            CobroDeposito deposito = new CobroDeposito()
            deposito.fechaDeposito = solicitud.fechaDeposito
            deposito.bancoOrigen = solicitud.banco
            deposito.cuentaDestino = solicitud.cuenta
            deposito.totalCheque = solicitud.cheque
            deposito.totalEfectivo = solicitud.efectivo
            deposito.folio = solicitud.folio
            deposito.cobro = cobro
            deposito.save failOnError: true, flush: true
            if(['CRE','CHE','JUR'].contains(cobro.tipo)) {
                registrarIngreso(cobro)
            }
        }
        return cobro
    }

    def registrarIngreso(Cobro cobro){
        if(cobro.ingreso == null && cobro.primeraAplicacion) {
            Empresa empresa = Empresa.first()
            Date fecha = cobro.primeraAplicacion
            if(cobro.deposito) {
                CobroDeposito deposito = cobro.deposito
                MovimientoDeCuenta mov = new MovimientoDeCuenta()
                mov.sucursal = cobro.sucursal.nombre
                mov.referencia = "Deposito: ${deposito.folio} "
                mov.tipo = cobro.tipo
                mov.fecha = fecha
                mov.formaDePago = cobro.formaDePago
                mov.comentario = "Deposito ${cobro.tipo} ${cobro.sucursal.nombre} "
                mov.cuenta = deposito.cuentaDestino
                mov.afavor = empresa.nombre
                mov.importe = cobro.importe
                mov.moneda = deposito.cuentaDestino.moneda
                mov.concepto = 'VENTAS'
                mov.conceptoReporte = "Deposito suc: ${mov.sucursal}"
                int mpa = Periodo.obtenerMes(cobro.primeraAplicacion)
                int mdp = Periodo.obtenerMes(deposito.fechaDeposito)
                mov.porIdentificar = mpa != mdp

                mov.save failOnError: true, flush: true
                deposito.ingreso = mov
                cobro.save flush: true
                return mov

            } else if(cobro.transferencia) {
                log.debug('Transferencia: {}', cobro.transferencia)
                CobroTransferencia transferencia = cobro.transferencia
                MovimientoDeCuenta mov = new MovimientoDeCuenta()
                mov.sucursal = cobro.sucursal.nombre
                mov.referencia = "Deposito: ${transferencia.folio} "
                mov.tipo = cobro.tipo
                mov.fecha = fecha
                mov.formaDePago = cobro.formaDePago
                mov.comentario = "Transferencia ${cobro.tipo} ${cobro.sucursal.nombre} "
                int mpa = Periodo.obtenerMes(cobro.primeraAplicacion)
                int mdp = Periodo.obtenerMes(transferencia.fechaDeposito)
                mov.porIdentificar = mpa != mdp
                mov.cuenta = transferencia.cuentaDestino
                mov.afavor = empresa.nombre
                mov.importe = cobro.importe
                mov.moneda = transferencia.cuentaDestino.moneda
                mov.concepto = 'VENTAS'
                mov.conceptoReporte = "Deposito suc: ${mov.sucursal}"
                mov.save failOnError: true, flush: true
                transferencia.ingreso = mov;
                cobro.save flush: true
                return mov
            }
        }
    }

    /**
     * Genera la descripcion adecuada para el estado de cuenta
     *
     * @param ingreso
     */
    def generarConceptoDeReporte(MovimientoDeCuenta ingreso) {
        String c = "Deposito suc: ${ingreso.sucursal}"
        ingreso.conceptoReporte = c
    }
}
