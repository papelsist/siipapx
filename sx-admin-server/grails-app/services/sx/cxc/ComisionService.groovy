package sx.cxc

import com.luxsoft.utils.MonedaUtils
import org.apache.commons.lang3.exception.ExceptionUtils

class ComisionService {

    public List<Comision> generarComisionesCobrador(String tipo, Date fechaIni, Date fechaFin){
        def origen = 'CRE'

        String hql = "select a.cuentaPorCobrar.id, a.fecha, a.importe from AplicacionDeCobro a " +
                "where a.fecha between ? and ? " +
                "  and a.cuentaPorCobrar.tipo = ? " +
                " and formaDePago not in('DEVOLUCION','BONIFICACION','PAGO_DIF') " +
                " and a.cuentaPorCobrar.cliente.credito.cobrador.sw2 != 1 " +
                " and a.cobro.anticipo = false and a.cobro.enviado = false " +
                " and date(a.fecha) = date(a.cobro.primeraAplicacion)"

        if (tipo == 'VEN') {
            hql = "select a.cuentaPorCobrar.id, a.fecha, a.importe, a.cobro.primeraAplicacion, a.cuentaPorCobrar.fecha" +
                    " from AplicacionDeCobro a " +
                    "where a.fecha between ? and ? " +
                    "  and a.cuentaPorCobrar.tipo = ? " +
                    " and formaDePago not in('DEVOLUCION','BONIFICACION','PAGO_DIF') " +
                    " and a.cuentaPorCobrar.cliente.vendedor.sw2 != 1 " +
                    " and a.cobro.anticipo = false " +
                    " and date(a.fecha) = date(a.cobro.primeraAplicacion)"
        }

        List rows = AplicacionDeCobro.findAll(hql,[fechaIni, fechaFin, origen])
        if(tipo == 'VEN') {
            rows = rows.findAll { (it[3] - it[4]) <= 30} // Filtrando todos los menores/igual a 30 de atraso en pago
        }
        Map<String, Object> cmap = rows.groupBy {it[0]}
        List<Comision> comisiones = []
        cmap.each { it ->
            String id = it.key
            def data = it.value
            BigDecimal pagoComisionable = data.sum { r -> r[2]}
            Date pagoComision = data.first()[1]
            CuentaPorCobrar cxc = CuentaPorCobrar.get(id);
            Comision comision = new Comision()
            comision.cxc = cxc
            comision.cliente = cxc.cliente.nombre
            comision.comisionista = cxc.cliente.credito.cobrador.nombres
            comision.clave = cxc.cliente.credito.cobrador.sw2
            comision.sucursal = cxc.sucursal.nombre
            comision.documentoTipo = cxc.tipo
            comision.documento = cxc.documento
            comision.fechaDocto = cxc.fecha
            comision.total = cxc.total
            comision.atraso = cxc.atraso
            comision.comision = cxc.cliente.credito.cobrador.comision
            if( cxc.cliente.clave == 'U050008') {
                comision.comision = 0.05
            }
            comision.tipo = tipo
            comision.fechaIni = fechaIni
            comision.fechaFin = fechaFin

            comision.fechaCobro = pagoComision
            comision.pagoComision = new Date()
            comision.pagoComisionable = pagoComisionable
            BigDecimal comisionImporte = comision.pagoComisionable * (comision.comision/100)
            comision.comisionImporte = MonedaUtils.round(comisionImporte)
            try{
                comision.save failOnError: true, flush: true
                comisiones << comision
            }catch (Exception sx) {
                log.error(ExceptionUtils.getRootCauseMessage(sx))
            }
        }
        return comisiones

    }
}


