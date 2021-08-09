package sx.cxc

import com.luxsoft.cfdix.v33.NotaDeCargoBuilder
import com.luxsoft.utils.MonedaUtils
import grails.gorm.transactions.Transactional
import lx.cfdi.v33.Comprobante
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.cfdi.CfdiTimbradoService
import sx.core.AppConfig
import sx.core.Folio

@Transactional
class NotaDeCargoService {

    CfdiTimbradoService cfdiTimbradoService

    CfdiService cfdiService

    NotaDeCargoBuilder notaDeCargoBuilder

    NotaDeCargo save(NotaDeCargo nota){
        if(nota.id) {
            throw new RuntimeException('Nota ya ha sido salvada')
        }
        nota.folio = Folio.nextFolio('NOTA_DE_CARGO', nota.serie)
        nota.sucursal = AppConfig.first().sucursal
        nota.createUser = 'admin'
        nota.updateUser = 'admin'
        nota.usoDeCfdi = nota.usoDeCfdi ?: 'G01'
        actualizarPartidas(nota)
        if (nota.tipoDeCalculo == 'PORCENTAJE') {
            calcularPorentaje(nota)
        } else {
            calcularProrrateo(nota)
        }
        if(nota.partidas) {
            nota.importe = nota.partidas.sum 0.0, {it.importe}
            nota.impuesto = nota.partidas.sum 0.0, {it.impuesto}
            nota.total = nota.partidas.sum 0.0, {it.total}
        } else {
            if(nota.total <= 0.0) {
                throw new RuntimeException('Nota de cargo sin conceptos debe tener el importe debe ser mayor a 0')
            }
            generarConceptoUnico(nota)
            nota.importe = nota.partidas.sum 0.0, {it.importe}
            nota.impuesto = nota.partidas.sum 0.0, {it.impuesto}
            nota.total = nota.partidas.sum 0.0, {it.total}
        }
        generarCuentaPorCobrar(nota)
        nota.save failOnError: true, flush:true
    }

    NotaDeCargo update(NotaDeCargo nota){
        if(nota.id == null) {
            throw new RuntimeException('Nota de cargo no se ha salvado')
        }
        nota.updateUser = 'admin'
        if(nota.tipo != 'CHE') {
            if (nota.tipoDeCalculo == 'PORCENTAJE') {
                calcularPorentaje(nota)
            } else {
                calcularProrrateo(nota)
            }
            nota.importe = nota.partidas.sum 0.0, {it.importe}
            nota.impuesto = nota.partidas.sum 0.0, {it.impuesto}
            nota.total = nota.partidas.sum 0.0, {it.total}
        } else {  // Cheques devueltos
            log.info('Actualizando nota de cargo tipo CHE')
            if(!nota.partidas) {
                generarConceptoUnico(nota)
            } else {
                actualizarConceptoUnico(nota)
            }
            nota.importe = nota.partidas.sum 0.0, {it.importe}
            nota.impuesto = nota.partidas.sum 0.0, {it.impuesto}
            nota.total = nota.partidas.sum 0.0, {it.total}

        }
        actualizarCuentaPorCobrar(nota)
        nota.save failOnError: true, flush:true
    }

    private actualizarPartidas(NotaDeCargo nota){
        nota.partidas.each { NotaDeCargoDet det ->
            if (det.cuentaPorCobrar) {
                CuentaPorCobrar cxc = det.cuentaPorCobrar
                det.documento = cxc.documento
                det.documentoFecha = cxc.fecha
                det.documentoTipo= cxc.tipo
                det.documentoTotal = cxc.total
                det.documentoSaldo = cxc.saldo
                det.sucursal = cxc.sucursal.nombre
            }
            det.comentario = nota.comentario
        }
    }

    def calcularProrrateo(NotaDeCargo nota) {
        assert nota.total >0 , 'Nota de cargo requiere total para proceder Total registrado: ' + nota.total
        nota.cargo = 0.0;
        BigDecimal importe = nota.total

        List<CuentaPorCobrar> facturas = nota.partidas.collect{ it.cuentaPorCobrar}
        NotaDeCargoDet sinSaldo = nota.partidas.find{it.cuentaPorCobrar.getSaldo() == 0.0}
        log.debug('Se encontro una factura con saldo {}', sinSaldo)
        boolean sobreSaldo = sinSaldo == null

        BigDecimal base = facturas.sum 0.0,{ item -> sobreSaldo ? item.getSaldo() : item.getTotal()}

        log.debug("Importe a prorratear: ${importe} Base del prorrateo ${base} Tipo ${sobreSaldo ? 'SOBR SALDO': 'SOBRE TOTAL'}")

        nota.partidas.each {  NotaDeCargoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? cxc.getSaldo(): cxc.total
            def por = monto / base
            def asignado = MonedaUtils.round(importe * por)
            det.importe = MonedaUtils.calcularImporteDelTotal(asignado)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto
        }
        return nota
    }

    def calcularPorentaje(NotaDeCargo nota) {
        log.debug('Generando Nota de cargo por el {}%', nota.cargo)
        def cargo = nota.cargo / 100
        boolean sobreSaldo = true
        nota.partidas.each {  NotaDeCargoDet det ->
            CuentaPorCobrar cxc = det.cuentaPorCobrar
            def monto = sobreSaldo ? det.cuentaPorCobrar.getSaldo() : det.cuentaPorCobrar.getTotal()
            def total = MonedaUtils.round(monto * cargo)
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
        }
        return nota
    }

    def generarCuentaPorCobrar(NotaDeCargo nota) {
        CuentaPorCobrar cxc = new CuentaPorCobrar()
        cxc.sucursal = nota.sucursal
        cxc.cliente = nota.cliente
        cxc.tipoDocumento = 'NOTA_DE_CARGO'
        cxc.importe = nota.importe
        cxc.descuentoImporte = 0.0
        cxc.subtotal = nota.importe
        cxc.impuesto = nota.impuesto
        cxc.total  = nota.total
        cxc.formaDePago = nota.formaDePago
        cxc.moneda = nota.moneda
        cxc.tipoDeCambio = nota.tipoDeCambio
        cxc.comentario = nota.comentario
        cxc.tipo = nota.tipo
        cxc.documento = nota.folio
        cxc.fecha = new Date()
        cxc.createUser = nota.createUser
        cxc.updateUser = nota.updateUser
        cxc.comentario = nota.comentario
        nota.cuentaPorCobrar = cxc
        cxc.save()
        return nota
    }

    def actualizarCuentaPorCobrar(NotaDeCargo nota) {
        CuentaPorCobrar cxc = nota.cuentaPorCobrar
        cxc.importe = nota.importe
        cxc.impuesto = nota.impuesto
        cxc.subtotal = nota.importe
        cxc.total = nota.total
        cxc.save()
    }

    def delete(NotaDeCargo nota){
        if(nota.cfdi) {
          throw new NotaDeCargoException("Nota de cargo ya timbrada no se puedd eliminar")
        }
        def cxc = nota.cuentaPorCobrar
        nota.cuentaPorCobrar = null
        nota.delete flush: true
        cxc.delete flush: true
    }

    def generarCfdi(NotaDeCargo nota) {
        Comprobante comprobante = this.notaDeCargoBuilder.build(nota);
        Cfdi cfdi = cfdiService.generarCfdi(comprobante, 'I', 'NOTA_CARGO')
        nota.cuentaPorCobrar.cfdi = cfdi
        nota.cuentaPorCobrar.uuid = cfdi.uuid
        nota.cfdi = cfdi
        nota.save flush: true
        //Cxc

        return nota
    }

    def timbrar(NotaDeCargo nota){
        try {
            if(!nota.cfdi) {
                nota = generarCfdi(nota)
            }
            def cfdi = nota.cfdi
            cfdi = cfdiTimbradoService.timbrar(cfdi)
            return nota
        } catch (Throwable ex){
            ex.printStackTrace()
            throw  new NotaDeCargoException(ExceptionUtils.getRootCauseMessage(ex))
        }
    }

    def generarConceptoUnico(NotaDeCargo nota) {
        if(!nota.partidas) {
            log.info('Generando concepto unico para nora tipo {} N.Cargo ', nota.tipo)
            NotaDeCargoDet det = new NotaDeCargoDet()
            det.comentario = nota.comentario

            BigDecimal total = nota.total
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto

            det.documento = nota.folio
            det.documentoTipo = nota.tipo
            det.documentoSaldo = 0.0
            det.documentoTotal = 0.0
            det.documentoFecha = nota.fecha
            det.sucursal = nota.sucursal.nombre
            nota.addToPartidas(det)
        }
    }

    def actualizarConceptoUnico(NotaDeCargo nota) {
        if(nota.partidas && nota.tipo == 'CHE') {
            NotaDeCargoDet det = nota.partidas[0]
            det.comentario = nota.comentario

            BigDecimal total = nota.total
            det.total = total
            det.importe = MonedaUtils.calcularImporteDelTotal(total)
            det.impuesto = MonedaUtils.calcularImpuesto(det.importe)
            det.total = det.importe + det.impuesto

            det.documento = nota.folio
            det.documentoTipo = nota.tipo
            det.documentoSaldo = nota.total
            det.documentoTotal = nota.total
            det.documentoFecha = nota.fecha
            det.sucursal = nota.sucursal.nombre
        }
    }

}

class NotaDeCargoException  extends RuntimeException {

    NotaDeCargoException(String message) {
        super(message)
    }

}
