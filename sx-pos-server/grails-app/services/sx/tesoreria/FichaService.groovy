package sx.tesoreria

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.AppConfig
import sx.core.Folio
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro

@Transactional
class FichaService {

    @Subscriber()
    def onSalvarCorteCobranza(CorteCobranza corte) {
        switch (corte.formaDePago) {
            case 'CHEQUE':
                log.info('CorteCobranza  de tipo CHEQUE detectado, generando fichas...')
                generarFichasDeCheque(corte.fecha, corte.tipoDeVenta)
                break
            case 'EFECTIVO':
                log.info('CorteCobranza  de tipo EFECTIVO detectado, generando fichas...')
                buildFicha('EFECTIVO', corte.tipoDeVenta,corte.deposito)
        }

    }

    def generarFichasDeCheque(Date fecha, String tipo) {

        String hql = "select a.cobro from AplicacionDeCobro a " +
                " where date(a.cobro.primeraAplicacion) = ? " +
                " and a.cobro.formaDePago = ? " +
                " and a.cuentaPorCobrar.tipo = ?" +
                " and a.cobro.cheque.ficha is null"

        Set<Cobro> cobros = Cobro.executeQuery(hql, [fecha, 'CHEQUE', tipo])

        if (tipo == 'CON') {
            log.debug('Tipo de contado buscando cambios de cheque por efectivo')
            List cambios = Cobro.executeQuery(
                    "from Cobro c where date(c.primeraAplicacion) = ? " +
                            " and c.cheque.cambioPorEfectivo = true " +
                            " and c.cheque.ficha is null",
                    [fecha])
            log.debug('Cobros de cambioPorEfectivo: {}', cambios.size())
            cobros.addAll(cambios)
        }
        Set<Cobro> mismoBanco = cobros.findAll { it.cheque.bancoOrigen.nombre == 'SCOTIABANK'}
        Set<Cobro> otrosBancos = cobros.findAll { it.cheque.bancoOrigen.nombre != 'SCOTIABANK'}
        armarFichas(new ArrayList(mismoBanco), 'MISMO_BANCO', tipo)
        armarFichas(new ArrayList(otrosBancos), 'OTROS_BANCOS', tipo)
    }

    def armarFichas(List<Cobro> cobros, String tipo, String origen) {
        int folio = 1
        List grupo = []
        for (int i = 0; i < cobros.size(); i++) {
            Cobro cobro = cobros.get(i)
            if(grupo.size() < 5) {
                grupo << cobro
            } else {
                generarFicha(tipo, origen, grupo)
                grupo = []
                grupo << cobro
            }
        }
        if (grupo) {
            generarFicha(tipo, origen, grupo)
        }
    }

    def generarFicha(String tipo, String origen, List<Cobro> grupo) {
        BigDecimal total = grupo.sum (0.0, {it.importe})
        def cuenta = CuentaDeBanco.where{numero == '16919455' }.find()
        assert cuenta, 'No existe la cuenta de banco para fichas'
        Ficha ficha = new Ficha()
        ficha.fecha = new Date()
        ficha.cuentaDeBanco = cuenta
        ficha.tipoDeFicha = tipo
        ficha.sucursal = AppConfig.first().sucursal
        ficha.origen = origen
        ficha.total = total
        ficha.folio = Folio.nextFolio('FICHAS','FICHAS')
        ficha.save failOnError: true, flush: true
        grupo.each {
            it.cheque.ficha = ficha
            it.save flush:true
        }
        log.debug('Fihca generada {}', ficha)
        return ficha
    }

    def buildFicha(String tipo, String origen, BigDecimal total) {
        def cuenta = CuentaDeBanco.where{numero == '16919455' }.find()
        assert cuenta, 'No existe la cuenta de banco para fichas'
        Ficha ficha = new Ficha()
        ficha.fecha = new Date()
        ficha.cuentaDeBanco = cuenta
        ficha.tipoDeFicha = tipo
        ficha.sucursal = AppConfig.first().sucursal
        ficha.origen = origen
        ficha.total = total
        ficha.folio = Folio.nextFolio('FICHAS','FICHAS')
        ficha.save failOnError: true, flush: true
        log.debug('Fihca generada {}', ficha)
        return ficha
    }

}
