package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.compras.DevolucionDeCompra
import sx.compras.DevolucionDeCompraDet
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Sucursal



/**
 * Created by Luis on 05/06/17.
 */
@Component
class ImportadorDevolucionCompra implements Importador,SW2Lookup {



    @Autowired
    ImportadorDeInventario importadorDeInventario

    @Autowired
    ImportadorDeProductos importadorDeProductos

    def importar(Date f1){

        importar(f1,f1)

    }



    def importar(Date ini, Date fin){

        def ids = leerRegistros("SELECT c.devolucion_id FROM sx_devolucion_compras c  where fecha between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            println( r)
            importar(r.devolucion_id)
        }
        return 'OK'
    }

    def importar(String devolucion_id){

        def movimientos=leerRegistros(QUERY,[devolucion_id])
        movimientos.each {dev ->



            DevolucionDeCompra devolucion=DevolucionDeCompra.where {sw2==dev.sw2}.find()
            Sucursal sucursal=buscarSucursal(dev.sucursal_id)

            Proveedor proveedor=Proveedor.where{sw2 == dev.proveedor_id}.find()


            if(!devolucion){
                devolucion=new DevolucionDeCompra()
            } else {
                devolucion.partidas.clear()
            }

            devolucion.sucursal=sucursal
            devolucion.proveedor=proveedor

            bindData(devolucion,dev)

            importarPartidas(devolucion)

            try {

                devolucion.save failOnError:true,flush:true

                importadorDeInventario.crearInventario(devolucion,'DEC')

            }catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))

                movimiento.save failOnError:true,flush:true
            }




        }
    }

    def importarPartidas(DevolucionDeCompra devolucion){
        List partidas = leerRegistros(QUERY_ROW,[devolucion.sw2])

        partidas.each{ row ->

            DevolucionDeCompraDet det = DevolucionDeCompraDet.where{sw2==row.sw2}.find()
            if(!det){
                det = new DevolucionDeCompraDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()



            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto



            bindData(det,row)
            devolucion.addToPartidas(det)
        }
    }



    String QUERY="""
    SELECT
        d.SUCURSAL_ID as sucursal_id,
        d.PROVEEDOR_ID as proveedor_id,
        d.DOCUMENTO as documento,
        d.FECHA as fecha,
        d.REFERENCIA as referencia,
        d.COMENTARIO as comentario,
        d.CREADO as dateCreated,
        d.MODIFICADO as lastUpdated,
        d.CREADO_USR as createUser,
        MODIFICADO_USR as udateUser,
        d.DEVOLUCION_ID as sw2
    FROM  sx_devolucion_compras d
    where devolucion_id= ?
    """

    String QUERY_ROW="""
    SELECT
        d.PRODUCTO_ID as producto_id,
        d.CANTIDAD as cantidad,
        d.COMENTARIO as comentario,
        CREADO as dateCreated,
        d.MODIFICADO as lastUpdated,
        d.INVENTARIO_ID as sw2
    FROM sx_inventario_dec d
    where DEVOLUCION_ID= ?

    """



}
