package sx.cxc

import grails.gorm.transactions.Transactional
import sx.core.AppConfig
import sx.core.Cliente
import sx.core.Sucursal
import sx.crm.BonificacionMC
import sx.crm.BonificacionMCAplicacion

@Transactional
class CajaBonificacionMCService {


    def generarDisponibles(Cliente cliente, BigDecimal porAplicar){
        Date fecha = new Date() // Date.parse('dd/MM/yyyy','15/10/2018')
        Sucursal sucursal = AppConfig.first().sucursal
        List<BonificacionMC> rows = BonificacionMC.findAll(
                'from BonificacionMC b ' +
                        ' where b.cliente = ? ' +
                        ' and (b.importe - b.aplicado -b.ajuste) > 0 ' +
                        ' and date(b.vencimiento) >= ? ',
                [cliente, fecha])
        List<BonificacionMCAplicacion> aplicaciones = []
        rows.each {

            BigDecimal aplicacionImp = 0.0
            if (it.importe >= porAplicar) {
                aplicacionImp = porAplicar
            } else
                aplicacionImp = it.importe


            Cobro cobro = generarCobro(sucursal, aplicacionImp, fecha, it)
            BonificacionMCAplicacion ap = new BonificacionMCAplicacion()
            ap.bonificacion = it
            ap.fecha = fecha
            ap.importe = aplicacionImp
            ap.sucursal = sucursal.nombre
            ap.cobro = cobro.id
            ap.save failOnError: true, flush: true
            aplicaciones << ap
            it.aplicado = it.aplicado + ap.importe
            it.save failOnError: true, flush: true

        }
        return aplicaciones
    }

    Cobro generarCobro(Sucursal sucursal, BigDecimal importe, Date fecha, BonificacionMC bonificacion) {
        Cobro cobro = new Cobro()
        cobro.cliente = bonificacion.cliente
        cobro.sucursal = sucursal
        cobro.importe = importe
        cobro.formaDePago = 'BONIFICACION'
        cobro.comentario =
                "BONIFICACION MEJORES CLIENTES (${bonificacion.ejercicio}-${bonificacion.mes})" +
                        " Vence:(${bonificacion.vencimiento.format('dd/MM/yyyy')})"
        cobro.fecha = fecha
        cobro.tipo = 'CON'
        cobro.referencia = "MC:${bonificacion.ejercicio}-${bonificacion.mes}"
        //cobro.sw2 = bonificacion.id
        cobro.save failOnError: true, flush: true
        return cobro
    }


}
