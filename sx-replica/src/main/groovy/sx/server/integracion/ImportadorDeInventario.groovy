package sx.server.integracion


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Inventario

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeInventario implements Importador, SW2Lookup{
    @Autowired
    ImportadorDeProductos importadorDeProductos

    def crearInventario(def movi,String tipo) {


        movi.partidas.each { det ->

            Inventario inventario = Inventario.where { sw2 == det.sw2 }.find()

            if (!inventario) {
                inventario = new Inventario()
            }

            inventario.sucursal=movi.sucursal
            inventario.producto=det.producto
            inventario.fecha=movi.fecha
            inventario.documento=movi.documento
            inventario.tipo=tipo
            inventario.cantidad=det.cantidad
            inventario.kilos=det.producto.kilos
            inventario.nacional=true
            inventario.sw2=det.sw2
            inventario.comentario=det.comentario

            inventario.save failOnError: true, flush: true

            det.inventario=inventario

            det.save failOnError: true, flush: true

        }




    }
}
