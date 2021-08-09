package sx.tesoreria

import grails.events.annotation.Publisher
import grails.gorm.transactions.Transactional
import sx.core.AppConfig
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro


@Transactional
class CorteCobranzaService {

    FichaService fichaService

    // @Publisher
    def salvarCorteCobranza(CorteCobranza resource) {
        resource.corte = new Date()
        if(resource.anticipoCorte) {
            resource.fechaDeposito = resource.fecha - 1
        } else {
            resource.fechaDeposito = resource.fecha
        }
        CorteCobranza corteCobranza = resource.save failOnError: true, flush: true
        fichaService.onSalvarCorteCobranza(corteCobranza)
        return corteCobranza

    }


    def prepararCorte( String formaDePago, String tipo, Date fecha) {
        log.debug('Calculando corte tipo {} ({}) para {}',  formaDePago, tipo, fecha.format('dd/MM/yyyy'))
        AppConfig config = AppConfig.first()
        CorteCobranza corte = new CorteCobranza()
        corte.formaDePago = formaDePago
        corte.sucursal = config.sucursal
        corte.tipoDeVenta = tipo
        corte.corte = new Date()
        corte.fecha = fecha
        corte.pagosRegistrados = getCobrosRegistrados(corte)
        corte.cortesAcumulado = getCortesAcumulados(corte)
        corte.cambiosDeCheques = getCambiosDeCheque(corte)
        return corte
    }

    def getCobrosRegistrados(CorteCobranza corte) {
        if (corte.formaDePago.startsWith('TARJETA')) {
            return Cobro.executeQuery(
                    "select sum(c.importe) from Cobro c where c.sucursal=? and (date(primeraAplicacion) = ? or (date(fecha)=? and anticipo is true)) and c.formaDePago like ?",
                    [corte.sucursal,corte.fecha,corte.fecha, 'TARJETA_%'])[0]?:00
        }
        if (corte.formaDePago == 'DEPOSITO') {
            log.debug(' Buscando aplicaciones para {} {} tipo  {} {}', corte.sucursal.nombre, corte.fecha.format('dd/MM/yyyy'), corte.tipoDeVenta, corte.formaDePago)
            Set<Cobro> cobros = Cobro.executeQuery(
                    "select a.cobro from AplicacionDeCobro a where date(a.cobro.primeraAplicacion) = ? and a.cobro.formaDePago like ? and a.cuentaPorCobrar.tipo = ?",
                [corte.fecha, 'DEPOSITO_%', corte.tipoDeVenta])
            def total = cobros.sum (0.0 , {it.importe})
            return total
        }
        if (corte.formaDePago == 'TRANSFERENCIA') {
            log.debug(' Buscando cobros para {} {} tipo  {} {}', corte.sucursal.nombre, corte.fecha.format('dd/MM/yyyy'), corte.tipoDeVenta, corte.formaDePago)
            Set<Cobro> cobros = Cobro.executeQuery(
                    "select a.cobro from AplicacionDeCobro a where date(a.cobro.primeraAplicacion) = ? and a.cobro.formaDePago = ? and a.cuentaPorCobrar.tipo = ?",
                    [corte.fecha, 'TRANSFERENCIA', corte.tipoDeVenta])
            def total = cobros.sum (0.0 , {it.importe})
            return total
        }
        if (corte.formaDePago == 'CHEQUE') {
            // log.debug(' Buscando cobros para {} {} tipo  {} {}', corte.sucursal.nombre, corte.fecha.format('dd/MM/yyyy'), corte.tipoDeVenta, corte.formaDePago)
            Set<Cobro> cobros = Cobro.executeQuery(
                    "select a.cobro from AplicacionDeCobro a " +
                            " where date(a.cobro.primeraAplicacion) = ? " +
                            " and a.cobro.formaDePago = ? " +
                            " and a.cuentaPorCobrar.tipo = ?" ,
                    [corte.fecha, 'CHEQUE', corte.tipoDeVenta])
            def total = cobros.sum (0.0 , {it.importe})
            return total
        }
        if (corte.formaDePago == 'EFECTIVO') {
            def pagosRegistrados = Cobro.executeQuery(
                    "select sum(c.importe) from Cobro c " +
                            " where c.sucursal=? " +
                            "  and (date(primeraAplicacion) = ? " +
                            " or (date(fecha)=? and anticipo is true)) "+
                            "  and c.formaDePago = ?",
                    [corte.sucursal,corte.fecha,corte.fecha, corte.formaDePago])[0]?:00
            return pagosRegistrados
        }

    }

    def getCortesAcumulados(CorteCobranza corte) {
        def cortes = CorteCobranza.where { tipoDeVenta == corte.tipoDeVenta && fecha == corte.fecha && formaDePago == corte.formaDePago}
        return cortes.sum (0.0, { it.deposito})
    }

    def getCambiosDeCheque(CorteCobranza corte) {
        if (corte.tipoDeVenta == 'COD')
            return 0.0
        def total = Cobro.executeQuery(
                "select sum(c.importe) from Cobro c " +
                        " where c.sucursal=? " +
                        "  and date(c.primeraAplicacion) = ? " +
                        "  and c.formaDePago = ?" +
                        "  and c.cheque.cambioPorEfectivo = true" ,
                [corte.sucursal,corte.fecha, 'CHEQUE'])[0]?:00
        if (corte.formaDePago == 'EFECTIVO') {
            total = total * -1
        }
        return total
    }

    def prepararCorteDeCheque(){
        log.debug('Preparando corte de cheques')
    }

    def getCobrosDeCheque(Date fecha, String tipoDeVenta) {

        String hql = "select a.cobro from AplicacionDeCobro a " +
                " where date(a.cobro.primeraAplicacion) = ? " +
                " and a.cobro.formaDePago = ? " +
                " and a.cuentaPorCobrar.tipo = ?" +
                " and a.cobro.cheque.ficha is null"
        Set<Cobro> cobros = Cobro.executeQuery(hql, [fecha, 'CHEQUE', tipoDeVenta])

        if (tipoDeVenta == 'CON') {
            List cambios = Cobro.executeQuery(
                    "from Cobro c where c.primeraAplicacion = ? " +
                            " and c.cheque.cambioPorEfectivo = true " +
                            " and c.cheque.ficha is null",
                    [fecha])
            cobros.addAll(cambios)
        }

        Set<Cobro> mismo = cobros.findAll { it.cheque.bancoOrigen.nombre == 'SCOTIABANK'}
        Set<Cobro> otros = cobros.findAll { it.cheque.bancoOrigen.nombre != 'SCOTIABANK'}
        Map res = [:]
        res['MISMO'] = mismo
        res['OTROS'] = otros
        return res
    }

    def agruparParaFichas(List<Cobro> cobros) {
        int folio = 1
        List grupo = []
        List fichas = []
        for (int i = 0; i < cobros.size(); i++) {
            Cobro cobro = cobros.get(i)
            if(grupo.size() < 5) {
                grupo << cobro
            } else {
                fichas << ['cantidad': grupo.size(), 'importe': grupo.sum(0.0, { it.importe})]
                grupo = []
                grupo << cobro
            }
        }
        if (grupo) {
            fichas << ['cantidad': grupo.size(), 'importe': grupo.sum(0.0, { it.importe})]
        }
        return fichas
    }

}
