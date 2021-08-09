package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Existencia
import sx.core.Producto
import sx.core.Sucursal

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeExistencia implements Importador,SW2Lookup {

    @Autowired
    ImportadorDeProductos importadorDeProductos

    def importar(def year,def mes){

        String select=QUERY+"  where year=? and mes=?"

           def existencias=leerRegistros(select,[year,mes])

            existencias.each {row ->

                Sucursal sucursal = buscarSucursal(row.sucursal_id)
                Producto producto = Producto.where {sw2 == row.producto_id}.find()
          /*      if(!producto) {
                    println("Importando producto $row.producto_id para la venta")
                    producto = importadorDeProductos.importar( row.producto_id)
                    assert producto, 'No fue posible importar el producto :' +row.producto_id
                }*/


                if(producto){

                    Existencia existencia=Existencia.where {anio == row.anio && mes == row.mes && producto == producto && sucursal == sucursal}.find()

                    if(!existencia){
                        existencia = new Existencia()
                    }else{
                        println "Existencia ya importada" + existencia.id
                    }
                    existencia.sucursal=sucursal
                    existencia.producto=producto
                    bindData(existencia,row)
                    existencia.save failOnError: true, flush: true
                }
            }
    }

    def importar(String sw2){

       // println "Importando"+sw2

        Existencia existencia = Existencia.where{ sw2 == sw2}.find() ?: new Existencia()
        def select = QUERY + ' where inventario_id = ?'
        def row = getSql().firstRow(select,[sw2])

        Sucursal sucursal = buscarSucursal(row.sucursal_id)

        Producto producto = Producto.where {sw2 == row.producto_id}.find()

     /*   if(!producto) {
            println("Importando producto $row.producto_id para la venta")
            producto = importadorDeProductos.importar( row.producto_id)
            assert producto, 'No fue posible importar el producto :' +row.producto_id
        }*/

        if(producto){
            existencia.sucursal=sucursal
            existencia.producto=producto
            bindData(existencia,row)

            try {
                existencia.save failOnError: true, flush: true
            }
            catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))
                errores.add(sw2)
            }
        }

    }





    static String QUERY="""
    SELECT
        sucursal_id,
        producto_id,
        year as anio,
        mes,
        nacional,
        kilos,
        e.PED_PENDTE as pedidosPendiente,
        cantidad,
        recorte,
        e.RECORTE_COMENTARIO as recorteComentario,
        e.RECORTE_FECHA as recorteFecha,
        inventario_id as sw2
    FROM sx_existencias e

    """
}
