package sx.compras

import grails.events.annotation.Publisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.Existencia
import sx.core.Folio
import sx.core.Inventario
import sx.core.Producto

@Transactional
class RecepcionDeCompraService { 

    def recibir(Compra compra, String username,remision,fechaR,comentario) {
        List<CompraDet> pendientes = compra.pendientes()
        log.debug('Generando COM automatico de compra {} con {} partidas pendientes', compra.folio, pendientes.size())

        if (!pendientes) {
            log.debug('Compra sin pendientes por recibir')
            //compra.pendiente = false
            //compra.save flush: true
            return null
        }

        RecepcionDeCompra com = new RecepcionDeCompra()
        com.sucursal = compra.sucursal
        com.fecha = new Date()
        com.compra = compra
        com.remision =remision
        com.fechaRemision=fechaR
        com.comentario = comentario
        com.proveedor = compra.proveedor
        pendientes.each { CompraDet item ->
            if (item.getPorRecibir() > 0) {
                RecepcionDeCompraDet det = new RecepcionDeCompraDet()
                det.producto = item.producto
                det.cantidad = item.getPorRecibir()
                det.compraDet = item
                com.addToPartidas(det)
                log.debug('Partida agregada de: {}', item)
            }
        }

        def res = this.save(com, username)

       // afectarInventario(res)


        return res
    }

    @Publisher('saveCom')
    def save(RecepcionDeCompra resource, String username) {
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.documento = Folio.nextFolio('COMS',serie)
            resource.createUser = username
        }
        resource.partidas.each { RecepcionDeCompraDet it ->
            it.comentario = resource.comentario
            it.kilos = getKilos(it.producto, it.cantidad)
        }
        resource.updateUser = username
        resource = resource.save failOnError: true, flush: true
        afectarInventario(resource)
        return resource
    }


    @Publisher
    def afectarInventario( RecepcionDeCompra com) {


        def renglon = 1;
        com.partidas.each { det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = com.sucursal
            inventario.documento = com.documento
            inventario.cantidad = det.cantidad
            inventario.comentario = det.comentario
            inventario.fecha = new Date()
            inventario.producto = det.producto
            inventario.tipo = 'COM'
            inventario.renglon = renglon
            det.inventario = inventario
            renglon++
        }
        com.fechaInventario = new Date()
        com.save flush: true
        actualizarExistencias(com)
    }

    def actualizarExistencias(RecepcionDeCompra com) {
        Date hoy = new Date()
        Integer ejercicio = hoy[Calendar.YEAR]
        Integer mes = hoy[Calendar.MONTH] + 1
        com.partidas.each { RecepcionDeCompraDet det ->
            Existencia existencia = Existencia.where { anio == ejercicio && mes == mes && producto == det.producto && sucursal == com.sucursal }.find()
           // assert existencia, "No existe la existencia del producto ${det.producto.clave} para ${ejercicio} - ${mes}"
            if(!existencia){
                existencia = new Existencia()
                existencia.anio = ejercicio
                existencia.mes = mes
                existencia.producto = det.producto
                existencia.sucursal = com.sucursal
                existencia.fecha = new Date()
                existencia.cantidad = 0.0
            }
            existencia.cantidad = existencia.cantidad + det.cantidad.abs()
            existencia.save failOnError: true, flush: true
            log.debug('Existencia actualizada: {}', existencia)
        }
    }

    private getKilos(Producto producto, BigDecimal cantidad) {
        def factor = producto.unidad == 'MIL' ? 1000 : 1;
        return producto.kilos * (cantidad/factor)
    }
}
