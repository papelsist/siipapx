package sx.cajas

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional

import com.luxsoft.utils.Periodo


import sx.core.Producto
import sx.core.Linea 
import sx.core.Clase
import sx.core.Marca
import sx.core.Existencia
import sx.inventario.Transformacion
import sx.inventario.TransformacionDet
import sx.core.Inventario
import sx.core.Sucursal
import sx.core.Folio
import sx.sat.ProductoSat
import sx.sat.UnidadSat





@Transactional
class CotizacionCajaService {




    def cerrar(cotizacionCaja){

        def sucursal = Sucursal.get(cotizacionCaja.sucursal.id)

    
        def producto = Producto.findByClave(cotizacionCaja.claveCaja)

        if(producto){
            println "Producto ya creado......"
            return []
        }

         producto = crearProducto(cotizacionCaja)

        def  transformacion = crearTransformacion(cotizacionCaja)

            transformacion = crearTransformacionDet(transformacion, cotizacionCaja, producto)
        
            transformacion.save failOnError:true, flush:true

            crearInventario(transformacion)

            crearExistencia(producto, sucursal, cotizacionCaja.piezas)
        
            cotizacionCaja.cerrada = true

            cotizacionCaja.save failOnError:true, flush:true

        return producto
         
    }

    def crearProducto(CotizacionCaja cotizacionCaja){
        
        Producto producto = new Producto()

	    producto.clave= cotizacionCaja.claveCaja
        producto.descripcion=cotizacionCaja.descripcionCaja
        producto.unidad = 'PZA'
        producto.modoVenta= 'B'		
        producto.presentacion = 'EXTENDIDO'
        if(cotizacionCaja.precioEspecialContado) {
            producto.precioContado  = cotizacionCaja.precioEspecialContado
        }else{
            producto.precioContado  = cotizacionCaja.precioPiezaContado
        }

        if(cotizacionCaja.precioEspecialCredito) {
            producto.precioCredito  = cotizacionCaja.precioEspecialCredito
        }else{
            producto.precioCredito = cotizacionCaja.precioPiezaCredito
        }
        producto.ancho = cotizacionCaja.ancho
        producto.largo = cotizacionCaja.largo
        producto.productoSat = ProductoSat.get(31);
        producto.unidadSat = UnidadSat.get(6);
        producto.ancho = 0.0
        producto.largo = 0.0
        producto.kilos = cotizacionCaja.kilos
        producto.gramos = cotizacionCaja.gramos
        producto.fechaLista = new Date()

        def linea = Linea.get('402880fc5e4ec411015e4ec62ffc0011')
        def clase = Clase.get('8a8a8165745a0a7501745ebc603300ba')
        def marca = Marca.get('402880fc5e4ec411015e4ec63624006a')
        producto.linea = linea
        producto.clase = clase
        producto.marca = marca

        producto.save failOnError:true, flush:true
    }

    def crearTransformacion(CotizacionCaja cotizacionCaja){

         Transformacion transformacion = new Transformacion()

         def sucursal = Sucursal.get(cotizacionCaja.sucursal.id)
     
         if(transformacion.id == null) {
            def serie =  cotizacionCaja.sucursal.clave
            transformacion.documento = Folio.nextFolio('TRANSFORMACION',serie)
            transformacion.fecha = new Date()
            transformacion.sucursal = sucursal
            transformacion.comentario = 'Transformacion a Cajas'
            transformacion.tipo = 'TRS'
        }

        return transformacion

    }
    
    def crearTransformacionDet(Transformacion transformacion, CotizacionCaja cotizacionCaja, Producto producto){
        
        TransformacionDet transformacionDetDestino = new TransformacionDet();
        transformacionDetDestino.producto = producto
        transformacionDetDestino.cantidad = cotizacionCaja.piezas
        transformacionDetDestino.comentario=  "Tranformacion a Cajas"

        transformacion.addToPartidas(transformacionDetDestino)

        TransformacionDet transformacionDetOrigen = new TransformacionDet();
        transformacionDetOrigen.producto = cotizacionCaja.producto
        transformacionDetOrigen.destino = transformacionDetDestino
        transformacionDetOrigen.cantidad = - cotizacionCaja.metrosLineales
        transformacionDetOrigen.comentario= "Tranformacion a Cajas"
        
        transformacion.addToPartidas(transformacionDetOrigen)

        

        return transformacion
    }

    def crearInventario(Transformacion transformacion){

        int renglon = 1
        transformacion.partidas.each{ det ->
             Inventario inventario = new Inventario()
            inventario.sucursal = transformacion.sucursal
            inventario.documento = transformacion.documento
            inventario.cantidad = det.cantidad
            inventario.comentario = det.comentario
            inventario.fecha = new Date()
            inventario.producto = det.producto
            inventario.tipo = 'TRS'
            inventario.kilos = 0.0
            inventario.renglon = renglon
            det.inventario = inventario
            inventario.save()
            renglon++
        }

        transformacion.fechaInventario  = new Date()
        transformacion.save()

    }

    def crearExistencia(Producto producto, Sucursal sucursal, BigDecimal cantidad){
        def periodo = new Periodo()
        def mes = periodo.currentMes()
        def year = periodo.currentYear()

        println "mes: ${mes} , year: ${year}"

        Existencia existencia =  Existencia.where {
            sucursal == sucursal &&
            producto == producto &&
            anio == year &&
            mes == mes }.find()

        if(!existencia){
            println "No hay existencia"
            existencia = new Existencia()
            existencia.sucursal = sucursal
            existencia.producto = producto
            existencia.anio = year
            existencia.mes = mes
            existencia.cantidad = cantidad
            existencia.fecha = new Date()
        }else{
            println existencia.producto.clave
            existencia.cantidad = existencia.cantidad + cantidad 
        }
        existencia.save()

        return existencia
    }

    def crearCotizacionLog(){
        
    }
    
    

      

}
