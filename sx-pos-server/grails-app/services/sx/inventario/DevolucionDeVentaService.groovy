package sx.inventario

import com.luxsoft.utils.MonedaUtils
import grails.gorm.transactions.Transactional
import sx.core.Folio
import sx.cxc.Cobro

@Transactional
class DevolucionDeVentaService {

    def save(DevolucionDeVenta rmd) {
        if(rmd.id == null) {
            def serie = rmd.sucursal.clave
            rmd.documento = Folio.nextFolio('RMD',serie)
        }
        def importeNeto = 0.0
        rmd.partidas.each {
            def cantidad = it.cantidad
            def factor = it.producto.unidad == 'MIL' ? 1000 : 1
            def precio = it.ventaDet.precio
            def subtotal = (cantidad/factor) * precio
            def descuento = it.ventaDet.descuento/100
            def descuentoImporte = subtotal * descuento
            def importe = MonedaUtils.round(subtotal - descuentoImporte)
            importeNeto = importeNeto + importe
           //  println "Importe en ventaDet ${it.ventaDet.subtotal} En rmd: ${importe}"
        }
        rmd.importe = importeNeto
        rmd.impuesto = MonedaUtils.calcularImpuesto(importeNeto)
        rmd.total = rmd.importe + rmd.impuesto
        if(rmd.total != rmd.venta.total){
            rmd.parcial=true
        }
        rmd.save failOnError: true, flush: true
        if(rmd.cobro == null) {
            generarCobro(rmd)
        }


    }

    def generarCobro(DevolucionDeVenta rmd){
        if (rmd.fechaInventario && rmd.venta.tipo != 'CRE') {
            Cobro cobro = new Cobro()
            cobro.setCliente(rmd.venta.cliente)
            cobro.setFecha(new Date())
            cobro.importe = rmd.total
            cobro.moneda = rmd.venta.moneda
            cobro.tipoDeCambio = rmd.venta.tipoDeCambio
            cobro.tipo = rmd.venta.tipo
            cobro.comentario = rmd.comentario
            cobro.createUser = rmd.createUser
            cobro.updateUser = rmd.updateUser
            cobro.sucursal = rmd.sucursal
            cobro.formaDePago = 'DEVOLUCION'
            rmd.cobro = cobro
            cobro.save()
            rmd.save()
        }
    }

}
