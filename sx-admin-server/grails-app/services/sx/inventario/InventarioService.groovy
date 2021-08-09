package sx.inventario

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Inventario
import sx.core.Venta
import sx.core.VentaDet

@Transactional
class InventarioService {

    @Subscriber
    def onFacturar(Venta venta){
        log.debug("Afectando inventario para factura: ${venta.statusInfo()}")
        if(venta.cuentaPorCobrar)
            afectarInventariosPorFacturar(venta)
    }

    private afectarInventariosPorFacturar(Venta venta){
        Venta.withNewTransaction {
            Venta factura = Venta.get(venta.id);
            int renglon = 1
            factura.partidas.each { VentaDet det ->
                Inventario inventario = new Inventario()
                inventario.sucursal = venta.sucursal
                inventario.documento = venta.cuentaPorCobrar.documento
                inventario.cantidad = det.cantidad.abs() * -1
                inventario.kilos = det.kilos;
                inventario.comentario = det.comentario
                inventario.fecha = venta.cuentaPorCobrar.fecha
                inventario.producto = det.producto
                inventario.tipo = 'FAC'
                inventario.tipoVenta = venta.cuentaPorCobrar.tipo
                inventario.renglon = renglon
                det.inventario = inventario
                renglon++
            }
        }
    }
}
