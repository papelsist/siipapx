package sx.inventario

import sx.core.AppConfig
import sx.core.Folio
import sx.core.Inventario
import sx.core.ExistenciaService
import com.luxsoft.utils.Periodo

import grails.gorm.transactions.Transactional

@Transactional
class ConteoService {

    ExistenciaService existenciaService

    def fijarConteo(){

        println "Fijando conteo"

        def fecha = new Date()
        
        def existencias = ExistenciaConteo.findAllByFecha(fecha)

        existencias.each{existencia ->

            def conteoExis = ConteoDet.findAll("from ConteoDet c where c.conteo.fecha = ? and c.producto = ?",[existencia.fecha,existencia.producto])
            def conteo = conteoExis.sum{it.cantidad}?: 0.00
           
            def diferencia =  conteo - existencia.cantidad 
  
            def ajuste = 0.00
            def existenciaFinal = existencia.cantidad 
            if (diferencia >= (-existencia.producto.ajuste) &&  diferencia <= existencia.producto.ajuste && diferencia != 0.00) {
                ajuste = diferencia
                existenciaFinal =  conteo?: 0.00
            }

            existencia.conteo = conteo?: 0.00
            existencia.diferencia = diferencia
            existencia.ajuste = ajuste
            existencia.existenciaFinal = existenciaFinal
            existencia.fijado = new Date()

            existencia.save failOnError: true, flush: true 
        }
    }

    def ajustePorConteo() {
        def fecha = new Date()
    
        def existencias = ExistenciaConteo.findAll("from ExistenciaConteo where date(fecha) = ? and  ajuste <> 0",[fecha])
        
        def sucursal = AppConfig.first().sucursal
        def serie = sucursal.clave

        def aju = new MovimientoDeAlmacen()

        aju.sucursal = sucursal
        aju.documento = Folio.nextFolio('MOVIMIENTO',serie)
        aju.fecha = new Date()
        aju.tipo = 'AJU'
        aju.porInventario = true
        aju.comentario = "Ajuste por inventario ${new Date()}"
        aju.createUser = "Por Conteo"
        aju.updateUser = "Por Conteo"


        existencias.each{ e ->

            def ajuDet = new  MovimientoDeAlmacenDet()

            ajuDet.producto = e.producto
            ajuDet.cantidad = e.ajuste
            ajuDet.comentario = "Por Inventario"

            aju.addToPartidas(ajuDet)

        }

        aju.save failOnError: true, flush:true

        def renglon = 1;

        aju.partidas.each { det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = sucursal
            inventario.documento = aju.documento
            inventario.cantidad = det.cantidad
            inventario.comentario = det.comentario
            inventario.fecha = aju.fecha
            inventario.producto = det.producto
            inventario.tipo = aju.tipo
            inventario.renglon = renglon
            inventario.save failOnError:true, flush:true
            det.inventario = inventario
            renglon++
        }
        aju.fechaInventario = new Date()
        aju.save failOnError: true, flush:true

        return aju
    }

    def recalculoPorAjuste() {
        def fecha = new Date()
        def existencias = ExistenciaConteo.findAll("from ExistenciaConteo where date(fecha) = ? and  ajuste <> 0",[fecha])
        def mes = Periodo.obtenerMes(new Date()) + 1
        def year = Periodo.obtenerYear(new Date())

        existencias.each{e ->
            println "Recalculando para  ${e.producto.clave} del periodo ${mes} - ${year}"
            ExistenciaService.recalcular(e.producto,year,mes)
        }
    }
}
