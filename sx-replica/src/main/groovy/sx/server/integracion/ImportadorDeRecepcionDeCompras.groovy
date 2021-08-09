package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.compras.Compra
import sx.compras.CompraDet
import sx.compras.RecepcionDeCompra
import sx.compras.RecepcionDeCompraDet
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Sucursal

/**
 * Created by rcancino on 17/10/16.
 */
@Component
class ImportadorDeRecepcionDeCompras implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeCompras importadorDeCompras

    @Autowired
    ImportadorDeInventario importadorDeInventario

    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Date ini, Date fin){
        logger.info("Importando recepciones de compra  del : ${ini.format('dd/MM/yyyy')} al ${ini.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select id from SX_ENTRADA_COMPRAS where date(fecha) between ? and ? ",[ini,fin])
        ids.each { r ->
            importar(r.id)
        }
    }

    def importar(String sw2){
        logger.info('Importando recepcion ' + sw2)
        String select = QUERY + " where id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){

        RecepcionDeCompra recepcion = RecepcionDeCompra.where{ sw2 == row.sw2}.find()


        if(!recepcion){
            recepcion = new RecepcionDeCompra()
        }else{
            recepcion.partidas.clear()
        }



        Compra compra= buscarCompra(row.compra_id)

        recepcion.compra =compra
        recepcion.sucursal = buscarSucursal(row.sucursal_id)
        recepcion.proveedor=compra.proveedor

        bindData(recepcion,row)


        importarPartidas(recepcion)



        try{
            recepcion.save failOnError:true, flush:true
            importadorDeInventario.crearInventario(recepcion,'COM')
            return recepcion
        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }
    }

    def importarPartidas(RecepcionDeCompra recepcion){
        List partidas = leerRegistros(QUERY_PARTIDAS,[recepcion.sw2])
        recepcion.partidas.clear()
        partidas.each{ row ->
            println "Importando Partida"+row.sw2

            RecepcionDeCompraDet det = new  RecepcionDeCompraDet()

            det.producto = buscarProducto(row.producto_id)

            det.compraDet = buscarCompraDet(row.compradet_id)

            bindData(det,row)

            recepcion.addToPartidas(det)

        }
    }

    def buscarCompra(def siipapId){

        Compra compra = Compra.where {sw2 == siipapId}.find()
        if (!compra) {
            println("Importando la compra")
           importadorDeCompras.importar(siipapId)
            compra = Compra.where {sw2 == siipapId}.find()
        }
        assert compra, 'No se ha importado la compra  $siipapId'
        return compra
    }

    def buscarCompraDet(def siipapId){
        CompraDet compraDet = CompraDet.where {sw2 == siipapId}.find()
        assert compraDet, "No se ha importado la compra unitaria $siipapId"
        return compraDet
    }



    static String QUERY  = """
    SELECT
        e.DOCUMENTO as documento,
        e.REMISION as remision,
        e.COMPRA_ID as compra_id,
        e.SUCURSAL_ID as sucursal_id,
        e.FECHA as fecha,
        e.COMENTARIO as comentario,e.ID as sw2,
        e.CREADO as dateCreated,
        e.MODIFICADO as lastUpdated,
        e.CREADO_USR as createUser,
        e.MODIFICADO_USR as updateUser
    FROM sx_entrada_compras e
    """



    static String QUERY_PARTIDAS ="""
    SELECT
        i.COMPRADET_ID as compradet_id,
        i.PRODUCTO_ID as producto_id,
        i.cantidad as cantidad,
        kilos,
        comentario,
        inventario_id as sw2,
        creado as dateCreated,
        modificado as lastUpdated
    FROM sx_inventario_com i
    where  RECEPCION_ID= ?

    """
}
