package sx.tesoreria

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.AppConfig
import sx.core.Empresa
import sx.core.Folio
import sx.cxc.Cobro

@Transactional
class FichaService {

    def preparar(FichasBuildCommand command) {

    }

    def registrarIngreso(Ficha ficha){
        assert !ficha.ingreso, 'Ingreso ya registrado'
        Empresa empresa = Empresa.first()
        MovimientoDeCuenta mov = new MovimientoDeCuenta()
        mov.referencia = "Ficha: ${ficha.folio} "
        mov.tipo = ficha.origen;
        mov.fecha = ficha.fecha
        mov.formaDePago = ficha.tipoDeFicha == 'EFECTIVO' ? 'EFECTIVO' : 'CHEQUE'
        mov.comentario = "Ficha ${ficha.tipoDeFicha} ${ficha.sucursal.nombre} "
        mov.cuenta = ficha.cuentaDeBanco
        mov.afavor = empresa.nombre
        mov.importe = ficha.total
        mov.moneda = mov.cuenta.moneda
        mov.concepto = 'VENTAS'
        mov.save failOnError: true, flush: true
        ficha.ingreso = mov;
        ficha.save()
    }


    def generar(FichasBuildCommand command) {
        switch (command.formaDePago) {
            case 'CHEQUE':
                generarFichasDeCheque(command.fecha, command.tipo, command.cuenta)
                break
            case 'EFECTIVO':
                generarFichaDeEfectivo(command.fecha, command.tipo, command.cuenta)

        }
    }

    def generarFichasDeCheque(Date fecha, String tipo, CuentaDeBanco cuenta) {

        String hql = "from Cobro a " +
                " where date(a.fecha) = ? " +
                " and a.formaDePago = ? " +
                " and a.tipo = ?" +
                " and a.cheque.ficha is null"

        Set<Cobro> cobros = Cobro.executeQuery(hql, [fecha, 'CHEQUE', tipo])
        Set<Cobro> mismoBanco = cobros.findAll { it.cheque.bancoOrigen.nombre == cuenta.descripcion}
        Set<Cobro> otrosBancos = cobros.findAll { it.cheque.bancoOrigen.nombre != cuenta.descripcion}
        armarFichas(fecha, new ArrayList(mismoBanco), 'MISMO_BANCO', tipo, cuenta)
        armarFichas(fecha, new ArrayList(otrosBancos), 'OTROS_BANCOS', tipo, cuenta)
    }

    def armarFichas(Date fecha, List<Cobro> cobros, String tipo, String origen, CuentaDeBanco cuenta) {
        int folio = 1
        List grupo = []
        for (int i = 0; i < cobros.size(); i++) {
            Cobro cobro = cobros.get(i)
            if(grupo.size() < 5) {
                grupo << cobro
            } else {
                generarFicha(fecha, tipo, origen, grupo, cuenta)
                grupo = []
                grupo << cobro
            }
        }
        if (grupo) {
            generarFicha(fecha, tipo, origen, grupo, cuenta)
        }
    }



    def generarFicha(Date fecha, String tipo, String origen, List<Cobro> grupo, CuentaDeBanco cuenta) {
        BigDecimal total = grupo.sum (0.0, {it.importe})
        // def cuenta = CuentaDeBanco.where{numero == '16919455' }.find()
        assert cuenta, 'No existe la cuenta de banco para fichas'
        Ficha ficha = new Ficha()
        ficha.fecha = fecha
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

    def generarFichaDeEfectivo(Date fecha, String origen, CuentaDeBanco cuenta) {

        BigDecimal total = Cobro.executeQuery(
                "select sum(c.importe) from Cobro c " +
                " where date(c.fecha) = ? " +
                "   and c.tipo = ? " +
                "   and c.formaDePago = 'EFECTIVO' ",
                [fecha,origen ])[0];
        // def cuenta = CuentaDeBanco.where{numero == '16919455' }.find()
        assert cuenta, 'No existe la cuenta de banco para fichas'

        Ficha ficha = new Ficha()
        ficha.fecha = fecha
        ficha.cuentaDeBanco = cuenta
        ficha.tipoDeFicha = 'EFECTIVO'
        ficha.sucursal = AppConfig.first().sucursal
        ficha.origen = origen
        ficha.total = total
        ficha.folio = Folio.nextFolio('FICHAS','FICHAS')
        ficha.save failOnError: true, flush: true
        log.debug('Fihca generada {}', ficha)
        return ficha
    }

}

