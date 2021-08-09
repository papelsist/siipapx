package sx.server.integracion

import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired

//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sx.compras.Compra
import sx.compras.CompraDet
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Sucursal

/**
 * Created by rcancino on 15/08/16.
 */
@Component
class ImportadorDeCompras implements  Importador, SW2Lookup{

    @Autowired
    ImportadorDeProductos importadorDeProductos

    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Date ini, Date fin){
        logger.info("Importando compras  del : ${ini.format('dd/MM/yyyy')} al ${ini.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select compra_id from SX_COMPRAS2 where date(fecha) between ? and ? ",[ini,fin])
        
        def errores = [];

        ids.each { row ->




            try {
                importar(row.compra_id)
            }
            catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))
                errores.add(row.compra_id)
            }
        }
        if(errores)
            println "Errores importando compras: " + errores.join(',');
    }


    def importarComprasIntegracion(){

        def compras= leerRegistros("select compra_id from compras_integracion",[])

        compras.each {row ->
            importar(row.compra_id)
        }

    }

    def importar(String sw2){

        println("Importando desde ele importar por -SW2")
        logger.info('Importando compra ' + sw2)
        String select = QUERY + " where compra_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)

    }



    def build(def row){

        println "Importando desde el build"
        def compra = Compra.where{ sw2 == row.sw2}.find()
        if(!compra){
            compra = new Compra()
        }
        bindData(compra,row)
        compra.sucursal = buscarSucursal( row.sucursal_id)
        compra.proveedor = buscarProveedor(row.proveedor_id)
        compra.nacional = !row.importacion
        importarPartidas(compra)


        try{

            compra.save failOnError:true, flush:true
        }catch (Exception ex){
            ex.printStackTrace()
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }

    }

    def importarPartidas(Compra compra){
        logger.info("Importando partidas para compra ${compra.folio}")
        validarProductos(compra)

        if(compra.partidas)
        compra.partidas.clear()
        
        List partidas = leerRegistros(QUERY_PARTIDAS,[compra.sw2])

        partidas.each{ row ->
            //logger.info("Importando partida: " + row)
            println("Importando partida: " )
            //CompraDet det = compra.partidas.find{ it.sw2 == row.sw2}
            CompraDet det = new  CompraDet()
            Producto producto = Producto.where {sw2 == row.producto_id}.find()
            if(!producto) {
                logger.info("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar( row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }
            det.producto = producto
            det.sucursal = buscarSucursal( row.sucursal_id)
            bindData(det,row)
            compra.addToPartidas(det)

        }
    }

    def validarProductos(Compra compra){
        def select ="select distinct producto_id from sx_compras2_det where compra_id = ?"
        leerRegistros(select,[compra.sw2]).each{  row ->
            def found = Producto.where {sw2 == row.producto_id}
            if (!found) {
                importadorDeProductos.importar(row.producto_id)
            }
        }
    }

    static String QUERY = """
      select
        compra_id as sw2,
        tipo,
        comentario,
        (case when c.DEPURACION is null then (SELECT max(x.DEPURACION) FROM sx_compras2_det x where x.COMPRA_ID=c.COMPRA_ID) else c.depuracion end) as ultimaDepuracion,
        (case when ((SELECT sum(x.solicitado) - sum(x.DEPURADO)  - ifnull((SELECT sum(i.cantidad) FROM sx_inventario_com i join sx_compras2_det d on(d.COMPRADET_ID=i.COMPRADET_ID) where d.compra_id= c.compra_id),0)
            FROM sx_compras2_det x where x.COMPRA_ID=c.COMPRA_ID))>0 then 'true' else 'false' end) as pendiente,
        entrega,
        fecha,
        folio,
        moneda,
        tc as tipoDeCambio,
        importe_bruto as importeBruto,
        importe_desc as importeDescuento,
        importe_neto as importeNeto,
        total,
        impuestos,
        sucursal_id,
        proveedor_id,
        nombre,
        consolidada,
        especial,
        importacion
        from sx_compras2 c
    """

    static String QUERY_PARTIDAS ="""
        select
            COMPRADET_ID as sw2,
            producto_id,
            clave,
            sucursal_id,
            comentario,
            costo,
            depuracion,
            precio,
            costo,
            desc1 as descuento1,
            desc2 as descuento2,
            desc2 as descuento3,
            desc2 as descuento4,
            desc2 as descuento5,
            desc2 as descuento6,
            descf as descuentoFinanciero,
            importe_bruto as importeBruto,
            importe_neto as importeNeto,
            solicitado,
            ancho,
            largo,
            depuracion,
            comentario
         from sx_compras2_det where compra_id = ?
    """
}
