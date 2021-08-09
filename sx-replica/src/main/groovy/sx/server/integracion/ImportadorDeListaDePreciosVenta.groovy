package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.compras.ListaDePreciosVenta
import sx.compras.ListaDePreciosVentaDet
import sx.core.Producto
import sx.core.Proveedor


/**
 * Created by rcancino on 12/10/16.
 */
@Component
class ImportadorDeListaDePreciosVenta implements Importador, SW2Lookup {
/*
    @Autowired
    ImportadorDeProductos importadorDeProductos

    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Date ini, Date fin){
        logger.info("Importando listad de precios de venta del : ${ini.format('dd/MM/yyyy')} al ${ini.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select lista_id from sx_lp_vent where date(creado) between ? and ? ",[ini,fin])
        ids.each { r ->
            importar(r.lista_id)
        }
    }

    def importar(Long sw2){
        logger.info('Importando lista ' + sw2)
        String select = QUERY + " where lista_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){
        def lista = ListaDePreciosVenta.where{ sw2 == row.sw2}.find()
        if(!lista){
            lista = new ListaDePreciosVenta()
        }
        bindData(lista,row)
        importarPartidas(lista)
        try{
            lista = lista.save failOnError:true, flush:true
            return lista
        }catch (Exception ex){logger.error(ExceptionUtils.getRootCauseMessage(ex))}
    }

    def importarPartidas(ListaDePreciosVenta lista){
        List partidas = leerRegistros(QUERY_PARTIDAS,[lista.sw2])
        lista.partidas.clear()
        partidas.each{ row ->
            ListaDePreciosVentaDet det = new  ListaDePreciosVentaDet()
            Producto producto = Producto.where {sw2 == row.producto_id}.find()
            if(!producto ){
                producto = importadorDeProductos.importar(row.producto_id)
            }
            assert producto, 'No existe el producto Id: ' + row.producto_id
            det.producto = producto
            det.proveedor = Proveedor.where {clave == row.prov_clave}.find()
            bindData(det,row)
            lista.addToPartidas(det)
        }
    }


*/
    static String QUERY  = """
        select
            lista_id as sw2,
            lista_id as folio,
            comentario as descripcion,
            aplicada,
            aplicada as inicio,
            autorizada as autorizacion,
            tc_dolares as tipoDeCambioDolar
            from sx_lp_vent
        """
    static String QUERY_PARTIDAS ="""
        select
            l.producto_id as producto_id,
            l.clave as clave,
            l.descripcion as descripcion,
            l.precio as precioContado,
            l.precio_credito as precioCredito,
            l.precio_anterior as precioAnteriorContado,
            l.precio_anterior_cre as precioAnteriorCredito,
            l.costo,
            l.costou as costoUltimo,
            l.incremento,
            l.factor as factorContado,
            l.factor_credito as factorCredito,
            l.prov_clave,
            l.kilos,
            l.gramos,
            l.*
            from sx_lp_vent_det l
            where lista_id = ?
        """
}
