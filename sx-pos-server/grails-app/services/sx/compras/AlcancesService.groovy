package sx.compras

import grails.web.databinding.DataBinder
import groovy.sql.Sql

import sx.core.AppConfig
import sx.core.Folio
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Sucursal

class AlcancesService implements DataBinder{

    def dataSource

    private static SQL = """
        SELECT 
        z.id as proveedor
        ,z.nombre
        ,CASE WHEN @SUCURSAL='%' THEN "TODAS" ELSE (SELECT S.NOMBRE FROM sucursal S WHERE S.activa IS TRUE AND S.ID LIKE @SUCURSAL ) END AS sucursal
        ,p.unidad
        ,L.linea
        ,C.clase
        ,M.marca
        ,P.clave
        ,P.descripcion
        ,P.kilos
        ,SUM(EXI) AS existencia
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE SUM(VTA)  END)  AS venta
        ,ROUND((ROUND(TO_DAYS('@FECHA_FIN')-TO_DAYS('@FECHA_INI'),0)),0)/30.4166 AS mesesPeriodo
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE (SUM(VTA)/(ROUND((ROUND(TO_DAYS('@FECHA_FIN')-TO_DAYS('@FECHA_INI'),0)),0)/30.4166)) END) AS promVta
        ,IFNULL(IFNULL(SUM(EXI),0)/IFNULL((CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE (SUM(VTA)/(ROUND((ROUND(TO_DAYS('@FECHA_FIN')-TO_DAYS('@FECHA_INI'),0)),0)/30.4166)) END),0),0) AS alcance
        ,SUM(PEND) AS comprasPendientes
        ,SUM(EXI)*P.KILOS/1000 AS existenciaEnToneladas
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE  (SUM(VTA)/(ROUND((ROUND(TO_DAYS('@FECHA_FIN')-TO_DAYS('@FECHA_INI'),0)),0)/30.4166)) END)*P.KILOS/1000 AS promVtaEnTonelada
        FROM (
        SELECT X.PRODUCTO_ID,X.CLAVE,SUM(X.CANTIDAD/(case when p.unidad ='MIL' then 1000 else 1 end)) AS EXI,0 AS VTA,0 AS PEND FROM EXISTENCIA X JOIN producto p ON(X.producto_id=p.ID) WHERE X.anio=YEAR('@FECHA_FIN') AND  X.MES=MONTH('@FECHA_FIN') AND   x.sucursal_id LIKE @SUCURSAL GROUP BY X.CLAVE,X.PRODUCTO_ID
        UNION
        SELECT X.PRODUCTO_ID,P.CLAVE,0,SUM((X.CANTIDAD*-1)/(case when p.unidad ='MIL' then 1000 else 1 end)) AS VTA,0 AS PEND FROM INVENTARIO X JOIN producto p ON(X.producto_id=p.ID) WHERE DATE(X.FECHA) BETWEEN '@FECHA_INI' AND '@FECHA_FIN' AND X.TIPO IN('FAC','DEV') AND   X.sucursal_id LIKE @SUCURSAL GROUP BY P.CLAVE,X.PRODUCTO_ID
        UNION
        SELECT X.PRODUCTO_ID,P.CLAVE,0,0,SUM(((X.SOLICITADO-X.DEPURADO)/(case when P.unidad ='MIL' then 1000 else 1 end))-IFNULL((SELECT SUM(I.CANTIDAD/(case when Y.unidad ='MIL' then 1000 else 1 end)) FROM recepcion_de_compra_det I JOIN producto Y ON(I.producto_id=Y.ID) WHERE I.compra_det_id=X.ID AND I.inventario_id IS NOT NULL),0)) AS PENDTE
        from compra_det X JOIN producto P ON(X.producto_id=P.ID) WHERE X.SUCURSAL_ID LIKE @SUCURSAL GROUP BY X.PRODUCTO_ID
        ) AS A
        JOIN producto P ON(A.PRODUCTO_ID=P.ID)
        join LINEA L on(L.ID=p.LINEA_ID)
        JOIN CLASE C ON(C.ID=P.CLASE_ID)
        JOIN marca M ON(M.ID=P.MARCA_ID)
        LEFT JOIN proveedor z on(p.proveedor_favorito_id=z.id)
        WHERE P.ACTIVO IS TRUE AND P.INVENTARIABLE IS TRUE 
        GROUP BY P.CLAVE 
        ORDER BY LINEA,CLASE
    """

    def generar(Date fechaInicial, Date fechaFinal, Integer meses) {
        // log.debug('Consulta de alcances: {}, {} ,{}' , fechaInicial, fechaFinal, meses)
        Alcance.executeUpdate("delete from Alcance ")
        Sucursal sucursal = AppConfig.first().sucursal
        String select = SQL.replaceAll('@SUCURSAL', "'${sucursal.id}'")
        .replaceAll('@FECHA_INI',fechaInicial.format('yyyy-MM-dd'))
        .replaceAll('@FECHA_FIN', fechaFinal.format('yyyy-MM-dd'))
        Sql sql = new Sql(dataSource)
        List res = []
        sql.eachRow(select, { row->
            Alcance alcance = new Alcance()
            alcance.fechaInicial = fechaInicial
            alcance.fechaFinal = fechaFinal
            alcance.meses = meses
            alcance.sucursal = row.sucursal
            alcance.clave = row.clave
            alcance.kilos = row.kilos
            alcance.unidad = row.unidad
            alcance.descripcion = row.descripcion
            alcance.linea = row.linea
            alcance.marca = row.marca
            alcance.clase = row.clase
            alcance.proveedor = row.proveedor
            alcance.nombre = row.nombre

            alcance.existencia = row.existencia
            alcance.existenciaEnToneladas  = row.existenciaEnToneladas

            alcance.venta = row.venta
            alcance.mesesPeriodo = row.mesesPeriodo
            alcance.promVta = row.promVta
            alcance.promVtaEnTonelada = row.promVtaEnTonelada

            alcance.alcance = row.alcance
            alcance.comprasPendientes = row.comprasPendientes
            alcance.promVtaEnToneladas = row.promVtaEnTonelada

            alcance.save failOnError: true, flush: true
            res << alcance
        })
        return res
    }

    def generarOrden(Proveedor proveedor, List<Alcance> alcances) {
        Compra compra = new Compra()
        compra.fecha = new Date()
        compra.sucursal = AppConfig.first().sucursal
        compra.proveedor = proveedor
        compra.comentario = " Compra automatica por alcance: ${alcances.get(0).fecha.format('dd/MM/yyyy')}"
        compra.folio = Folio.nextFolio('COMPRA','OFICINAS')
        if (proveedor.plazo)
            compra.entrega = compra.fecha + compra.proveedor.plazo
        alcances.each {

            CompraDet det = new CompraDet()
            println 'Buscando producto: '+ it.clave
            Producto producto = Producto.findByClave(it.clave)

            det.producto = producto
            det.comentario = compra.comentario
            det.sucursal = compra.sucursal
            det.solicitado = it.porPedir
            det.sw2 = ""
            compra.addToPartidas(det)
            it.comentario = "COMPRA ${compra.folio}"
            it.save flush: true
        }
        compra.actualizarStatus()
        compra.save failOnError: true, flush: true
        return compra
    }

    def actualizarMeses(int meses) {
        def res = Alcance.executeUpdate("update Alcance set meses = ? ", [meses])
        return res
    }
}


