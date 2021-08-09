package sx.tesoreria

import grails.gorm.transactions.Transactional
import sx.core.Empresa
import sx.core.Folio

@Transactional
class MovimientoDeTesoreriaService {

    public MovimientoDeTesoreria save(MovimientoDeTesoreria mov){
        if(!mov.id) {
            mov.folio = Folio.nextFolio('MOVIMIENTO_TES','OFICINAS')
            // registrarIngreso(mov)
        }
        mov.save failOnError: true, flush: true
    }

    def registrarIngreso(MovimientoDeTesoreria movTes){
        assert !movTes.movimiento, 'Ingreso ya registrado'
        Empresa empresa = Empresa.first()
        MovimientoDeCuenta mov = new MovimientoDeCuenta()
        mov.referencia = "Movimiento: ${movTes.folio} "
        mov.tipo = 'TES';
        mov.fecha = movTes.fecha
        mov.formaDePago = 'NOAPLICA'
        mov.comentario = "${movTes.concepto}  ${movTes.comentario}"
        mov.cuenta = movTes.cuenta
        mov.afavor = empresa.nombre
        mov.importe = movTes.importe
        mov.moneda = mov.cuenta.moneda
        mov.concepto = movTes.concepto
        movTes.movimiento = mov;

    }
}
