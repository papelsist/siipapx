package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Inventario
import sx.core.Producto
import sx.core.Sucursal
import sx.core.Venta
import sx.core.VentaDet
import sx.inventario.DevolucionDeVenta
import sx.inventario.DevolucionDeVentaDet

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeDevolucionDeVentas implements  Importador, SW2Lookup{

    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeVentas importadorDeVentas

    @Autowired
    ImportadorDeInventario importadorDeInventario

    def importar(Date f1){

        importar(f1, f1)
    }

    def importar(Date ini, Date fin){

        def ids = leerRegistros("SELECT DEVO_ID as devo_id FROM sx_devoluciones d  where fecha between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.devo_id)
        }
        return 'OK'
    }

    def importar(String devo_id){

        def devoluciones=leerRegistros(QUERY,[devo_id])
        devoluciones.each {dev ->
            DevolucionDeVenta devolucion=DevolucionDeVenta.where {sw2==dev.sw2}.find()
            Sucursal sucursal=buscarSucursal(dev.sucursal_id)
            Venta venta=Venta.where {sw2==dev.venta_id}.find()

            if(!venta){
                importadorDeVentas.importar(dev.venta_id)
            }

            if(!devolucion){
                devolucion=new DevolucionDeVenta()
            } else {
                devolucion.partidas.clear()
            }
            devolucion.sucursal=sucursal
            devolucion.venta=venta
            bindData(devolucion,dev)

            importarPartidas(devolucion)


            try {



                    devolucion.save failOnError:true,flush:true

                    importadorDeInventario.crearInventario(devolucion,'RMD')





            }catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))

                //movimiento.save failOnError:true,flush:true
            }



        }
    }

    def importarPartidas(DevolucionDeVenta devolucion){
        List partidas = leerRegistros(QUERY_ROW,[devolucion.sw2])


        partidas.each{ row ->

            DevolucionDeVentaDet det =DevolucionDeVentaDet.where{sw2==row.sw2}.find()
            if(!det){
                 det = new DevolucionDeVentaDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()
            VentaDet ventaDet=VentaDet.where{sw2==row.ventadet_id}.find()

            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

                det.producto = producto
                det.ventaDet=ventaDet

                bindData(det,row)

                // println('Agregando partida: ' + row.sw2+" ---- "+det.documento+" "+det.fecha)
                devolucion.addToPartidas(det)



        }


    }







    static String QUERY="""
        SELECT
            d.venta_id,
            d.NUMERO as documento,
            d.FECHA as fecha,
            d.VENTA_ID as venta_id,
            d.importe as importe,
            d.impuesto as impuesto,
            d.total as total,
            d.IMPORTE_CORTES as importeCortes,
            d.comentario,
            d.CREADO as dateCreated,
            d.MODIFICADO as lastUpdated,
            DEVO_ID as sw2,
            (SELECT sucursal_id FROM sx_ventas v where v.cargo_id=d.venta_id) as sucursal_id
        FROM sx_devoluciones d
        where devo_id= ?
    """

    String QUERY_ROW="""
        SELECT
            d.VENTADET_ID as ventadet_id,
            d.DEVO_ID as devolucionDeVentaId,
            d.PRODUCTO_ID as producto_id,
            d.CANTIDAD as cantidad,
            d.COSTO as costoDev,
            d.COSTO as ImporteCosto,
            d.COMENTARIO as comentario,
            d.CREADO as dateCreated,
            d.MODIFICADO as lastUpdated,
            INVENTARIO_ID as sw2
        FROM sx_inventario_dev d
        where DEVO_ID= ?
    """

}
