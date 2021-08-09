package sx.cxc

import grails.transaction.NotTransactional
import groovy.sql.Sql

import javax.sql.DataSource


class AntiguedadService {

    DataSource dataSource;

    @NotTransactional
    def List antiguedad() {
        Sql sql = new Sql(dataSource);
        return sql.rows(SQL);
    }

    /*
    static String SQL = """
    
    SELECT X.ID as clienteId, X.NOMBRE as cliente,C.PLAZO as plazo,C.linea_de_credito AS limiteDeCredito,CASE WHEN C.VENCE_FACTURA IS FALSE THEN 'REV' ELSE 'FAC' END AS tipoVencimiento,COUNT(F.ID) AS facturas
    ,MAX((CASE WHEN TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)>0 THEN TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO) ELSE 0 END)) AS atrasoMaximo
    ,SUM(F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) AS saldo
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))<1 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END) AS porVencer
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))>0 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END) AS vencido
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 1 AND 30 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END)  AS de1_30
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 31 AND 60 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END)  AS de31_60
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 61 AND 90 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END)  AS de61_90
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))>90 THEN (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)) ELSE 0 END)  AS mas90
    ,SUM((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0)))*100 /  ((SELECT SUM((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0))) 
         FROM cuenta_por_cobrar F JOIN cliente_credito C ON(F.cliente_id=C.cliente_id) WHERE F.FECHA>'2016/12/31'  AND  F.TIPO='CRE' AND  (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0))>0 )) AS part
    FROM  cuenta_por_cobrar F JOIN cliente_credito C ON(F.cliente_id=C.cliente_id) JOIN cliente X ON(F.CLIENTE_ID=X.ID)
    WHERE F.FECHA>'2014/12/31'  AND F.TIPO='CRE' AND 
    (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0))<>0 
    group by X.ID, C.PLAZO,C.linea_de_credito, C.VENCE_FACTURA 
    """
    */

    static String SQL = """
    SELECT X.ID as clienteId, X.NOMBRE as cliente,C.PLAZO as plazo,C.linea_de_credito AS limiteDeCredito,CASE WHEN C.VENCE_FACTURA IS FALSE THEN 'REV' ELSE 'FAC' END AS tipoVencimiento,COUNT(F.ID) AS facturas
    ,MAX((CASE WHEN TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)>0 THEN TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO) ELSE 0 END)) AS atrasoMaximo
    ,SUM((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) AS saldo
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))<1 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END) AS porVencer
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))>0 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END) AS vencido
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 1 AND 30 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END)  AS de1_30
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 31 AND 60 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END)  AS de31_60
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO)) BETWEEN 61 AND 90 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END)  AS de61_90
    ,SUM(CASE WHEN (TO_DAYS(CURRENT_DATE)-TO_DAYS(F.VENCIMIENTO))>90 THEN ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ) ELSE 0 END)  AS mas90
    ,SUM(((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio ))*100 /  ((SELECT SUM(((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio )) 
         FROM cuenta_por_cobrar F JOIN cliente_credito C ON(F.cliente_id=C.cliente_id) WHERE F.FECHA>'2016/12/31'  AND  F.TIPO='CRE' AND  (F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0))>0 )) AS part
    FROM  cuenta_por_cobrar F JOIN cliente_credito C ON(F.cliente_id=C.cliente_id) JOIN cliente X ON(F.CLIENTE_ID=X.ID)
    WHERE F.FECHA>'2017/01/01'  AND F.TIPO='CRE' AND f.id not in(select j.cxc_id from juridico j where j.cxc_id=f.id) and
    ((F.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM aplicacion_de_cobro B WHERE B.cuenta_por_cobrar_id=F.ID ),0) ) * F.tipo_de_cambio )<>0 
    group by X.ID, C.PLAZO,C.linea_de_credito, C.VENCE_FACTURA 
    """
}
