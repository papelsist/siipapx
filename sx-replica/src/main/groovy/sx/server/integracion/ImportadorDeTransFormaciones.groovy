package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Inventario
import sx.core.Producto
import sx.core.Sucursal
import sx.inventario.Transformacion
import sx.inventario.TransformacionDet

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeTransFormaciones implements Importador,SW2Lookup{
    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeInventario importadorDeInventario

    def importar(Date f1){

        importar(f1, f1)
    }


    def importar(Date ini, Date fin){

        def ids = leerRegistros("SELECT TRANSFORMACION_ID as transformacion_id FROM sx_transformaciones d  where DATE(fecha) between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            println( r)
            importar(r.transformacion_id)
        }
        return 'OK'
    }

    def importar(String transformacion_id){

        def movimientos=leerRegistros(QUERY,[transformacion_id])
        movimientos.each {transf ->
            Transformacion transformacion=Transformacion.where {sw2==transf.sw2}.find()
            Sucursal sucursal=buscarSucursal(transf.sucursal_id)


            if(!transformacion){
                transformacion=new Transformacion()
            } else {
                transformacion.partidas.clear()
            }
            transformacion.sucursal=sucursal


            bindData(transformacion,transf)

            importarPartidas(transformacion)


            try {

                transformacion.save failOnError:true,flush:true

                importadorDeInventario.crearInventario(transformacion,'TRS')

                ajusteDestino(transformacion)

            }catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))
            }



        }
    }

    def importarPartidas(Transformacion transformacion){
        List partidas = leerRegistros(QUERY_ROW,[transformacion.sw2])

        partidas.each{ row ->

            TransformacionDet det =TransformacionDet.where{sw2==row.sw2}.find()
            if(!det){
                det = new TransformacionDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto


            bindData(det,row)

            transformacion.addToPartidas(det)
        }
    }

    def ajusteDestino(Transformacion transformacion) {

    List partidas= leerRegistros(QUERY_ROW,[transformacion.sw2])
            partidas.each{row ->

                if(row.destino_sw2){

                    TransformacionDet destino=TransformacionDet.where {sw2==row.destino_sw2}.find()
                    TransformacionDet origen=TransformacionDet.where {sw2==row.sw2}.find()
                    origen.destino=destino
                    origen.save failOnError: true, flush: true
                }

        }

    }

    String QUERY="""
    SELECT sucursal_id as sucursal_id,
        documento,
        fecha,
        'TRS' as tipo,
        COMENTARIO,
        TRANSFORMACION_ID as sw2
        FROM sx_transformaciones t
        where TRANSFORMACION_ID=?

    """

    String QUERY_ROW="""
     SELECT
        i.PRODUCTO_ID as producto_id,
        i.CANTIDAD as cantidad,
        i.COMENTARIO as comentario,
        DESTINO_ID as destino_sw2,
        INVENTARIO_ID as sw2,
        trtip  as tipo
    FROM sx_inventario_trs i
    where TRANSFORMACION_ID=?

    """




}
