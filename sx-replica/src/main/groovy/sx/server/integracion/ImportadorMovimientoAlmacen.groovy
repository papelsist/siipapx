package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Producto
import sx.core.Sucursal
import sx.inventario.MovimientoDeAlmacen
import sx.inventario.MovimientoDeAlmacenDet
import  java.util.Date


/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorMovimientoAlmacen implements Importador,SW2Lookup {
    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeInventario importadorDeInventario

    def importar(Date f1){

        importar(f1, f1)

    }

    def importar(Date ini, Date fin){

        def ids = leerRegistros("SELECT MOVI_ID as movi_id FROM sx_movi d  where DATE(fecha) between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            println( r)
            importar(r.movi_id)
        }
        return 'OK'
    }

    def importar(String movi_id){

        def movimientos=leerRegistros(QUERY,[movi_id])
        movimientos.each {mov ->
            MovimientoDeAlmacen movimiento=MovimientoDeAlmacen.where {sw2==mov.sw2}.find()
            Sucursal sucursal=buscarSucursal(mov.sucursal_id)


            if(!movimiento){
                movimiento=new MovimientoDeAlmacen()
            } else {
                movimiento.partidas.clear()
            }
            movimiento.sucursal=sucursal

            bindData(movimiento,mov)

            importarPartidas(movimiento)


            try {

                movimiento.save failOnError:true,flush:true

                importadorDeInventario.crearInventario(movimiento,movimiento.tipo)

            }catch(Exception e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e))

                movimiento.save failOnError:true,flush:true
            }





        }
    }

    def importarPartidas(MovimientoDeAlmacen movimiento){
        List partidas = leerRegistros(QUERY_ROW,[movimiento.sw2])


        partidas.each{ row ->

            MovimientoDeAlmacenDet det =MovimientoDeAlmacenDet.where{sw2==row.sw2}.find()
            if(!det){
                det = new MovimientoDeAlmacenDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto


            bindData(det,row)

            movimiento.addToPartidas(det)
        }
    }

    String QUERY="""
        SELECT
            m.sucursal_id as sucursal_id,
            m.documento,
            m.fecha,
            m.CONCEPTO as tipo,
            m.PORINVENTARIO as porInventario,
            m.COMENTARIO,
            m.MOVI_ID as sw2
        FROM sx_movi m
        where MOVI_ID=?
    """

    String QUERY_ROW="""
        SELECT
            i.PRODUCTO_ID as producto_id,
            i.CANTIDAD as cantidad,
            i.COMENTARIO as comentario,
            i.INVENTARIO_ID as sw2,
            case when i.tipo_cis = 'PELERIA' then 'PAPELERIA' else i.tipo_cis end  as tipoCIS
        FROM sx_inventario_mov  i
        where i.MOVI_ID=?
    """

}
