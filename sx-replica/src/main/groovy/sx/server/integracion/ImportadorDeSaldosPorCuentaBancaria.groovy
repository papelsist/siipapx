package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.tesoreria.SaldoPorCuentaDeBanco


/**
 * 
 */
@Component
class ImportadorDeSaldosPorCuentaBancaria implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeMovimientosDeCuenta importadorDeMovimientosDeCuenta

    @Autowired
    ImportadorDeCobros importadorDeCobros
    

    def importar(Integer ejercicio, Integer mes){
        String select = QUERY.replaceAll("@EJERCICIO",ejercicio.toString()).replaceAll("@MES", mes.toString())
        def rows = leerRegistros(select, [])
        rows.each { row ->
            build(row)
        }
    }

    def build(def row){
        def cta = buscarCuentaDeBanco(row.cuenta_id)
        def saldo = SaldoPorCuentaDeBanco.where{ cuenta == cta && ejercicio == row.ejercicio && mes == row.mes }.find()
        if(saldo) 
            logger.info("Actualizando saldo $saldo")
        if(!saldo){
            logger.info("Generando saldo para ${cta} ${row.ejercicio} ${row.mes}")
            saldo = new SaldoPorCuentaDeBanco()
        }
        bindData(saldo,row)
        saldo.cuenta = cta
        saldo = saldo.save failOnError:true, flush:true
        return saldo
    }
    

    static String QUERY = """
        SELECT 
        cuenta_id,
        year(fecha) as ejercicio,
        month(fecha) as mes,
        SUM(IMPORTE) AS MOVIMIENTOS,
        sum(case when IMPORTE>0 then importe else 0 end) as egresos,
        sum(case when IMPORTE<0 then importe else 0 end) as ingresos,
        (SELECT SUM(IMPORTE) FROM sw_bcargoabono B WHERE B.CUENTA_ID=C.CUENTA_ID AND B.FECHA<'@EJERCICIO/@MES/01' AND B.CONCILIADO IS FALSE ) AS saldoInicial,
        (SELECT SUM(IMPORTE) FROM sw_bcargoabono B WHERE B.CUENTA_ID=C.CUENTA_ID AND B.FECHA<'@EJERCICIO/@MES/01' AND  B.CONCILIADO IS FALSE )+(SUM(IMPORTE)) AS saldoFinal
        FROM sw_bcargoabono c
        WHERE YEAR(C.FECHA)=@EJERCICIO  AND MONTH(C.FECHA)=@MES
        AND CONCILIADO IS FALSE
        group by year(fecha),month(fecha),CUENTA_ID
        ORDER BY CUENTA_ID
    """

    
}
