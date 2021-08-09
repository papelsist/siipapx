package sx.cxc

import com.luxsoft.utils.MonedaUtils

import grails.gorm.transactions.Transactional
import sx.core.Empresa
import sx.core.Folio
import sx.tesoreria.MovimientoDeCuenta


// @GrailsCompileStatic
@Transactional
class ChequeDevueltoService {



    ChequeDevuelto registrarChequeDevuelto(CobroCheque cobroCheque, Date fecha) {
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.cliente = cobroCheque.cobro.cliente
        cxc.sucursal = cobroCheque.cobro.sucursal
        cxc.formaDePago = cobroCheque.cobro.formaDePago
        cxc.documento = Folio.nextFolio('CHEQUE_DEVUELTO', cxc.tipo)
        cxc.comentario = 'Cargo por cheque devuelto'
        cxc.fecha = fecha
        cxc.tipo = 'CHE'
        cxc.tipoDocumento = 'CHEQUE_DEVUELTO'
        cxc.importe = MonedaUtils.calcularImporteDelTotal(cobroCheque.cobro.importe)
        cxc.subtotal = cxc.importe
        cxc.impuesto = MonedaUtils.calcularImpuesto(cxc.importe)
        cxc.total = cobroCheque.cobro.importe
        cxc.createUser = 'PENDIENTE'
        cxc.updateUser = 'PENDIENTE'
        cxc.save failOnError: true, flush: true

        ChequeDevuelto che = new ChequeDevuelto()
        che.comentario = ''
        che.folio = cxc.documento
        che.nombre = cxc.cliente.nombre
        che.cxc = cxc
        che.cheque = cobroCheque
        che.createUser = cxc.createUser
        che.updateUser = cxc.updateUser
        MovimientoDeCuenta egreso = registrarEgreso(che)
        egreso.save failOnError: true, flush: true
        che.egreso = egreso;
        che.save failOnError: true, flush: true
        return che

    }

    def registrarEgreso(ChequeDevuelto chequeDevuelto) {

        if(chequeDevuelto.egreso) return
        assert chequeDevuelto.cheque.ficha, "Se requiere la ficha de deposito para el Cobro: ${chequeDevuelto.cheque.cobro.id}"
        Empresa empresa = Empresa.first()
        MovimientoDeCuenta mov = new MovimientoDeCuenta()
        mov.referencia = "${chequeDevuelto.cheque.numero} "
        mov.tipo = 'CHE';
        mov.fecha = chequeDevuelto.cxc.fecha
        mov.formaDePago = 'CHEQUE'
        mov.comentario = "CHEQUE DEVUELTO:  ${chequeDevuelto.cxc.sucursal.nombre} "
        mov.sucursal = chequeDevuelto.cxc.sucursal.nombre
        mov.conceptoReporte = "Cargo por cheque devuelto suc: ${chequeDevuelto.cxc.sucursal.nombre}"
        mov.cuenta = chequeDevuelto.cheque.ficha.cuentaDeBanco
        mov.afavor = empresa.nombre
        mov.importe = chequeDevuelto.cxc.total * -1
        mov.moneda = mov.cuenta.moneda
        mov.concepto = 'CHEQUE_DEVUELTO'
        return mov
    }

    ChequeDevuelto save(ChequeDevuelto chequeDevuelto) {
        chequeDevuelto.folio = Folio.nextFolio('CHEQUE_DEVUELTO','CHE')
        return chequeDevuelto.save(failOnError: true, flush: true)
    }

    ChequeDevuelto update(ChequeDevuelto chequeDevuelto) {
        return chequeDevuelto.save(failOnError: true, flush: true)
    }
}
