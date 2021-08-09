package sx.integracion

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.object.SqlUpdate
import org.springframework.stereotype.Component
import sx.core.Sucursal
import sx.cxc.Cobro


@Component
@Slf4j
class CuentasPorCobrarIntegration implements  Integracion{

    static String SQL = "select count(*) as registros, ifNull(sum(importe),0) as control from cobro where date(fecha) = ? and sucursal_id = ?"

    def validar(Date fecha, Sucursal sucursal) {
        IntegracionLog integracionLog = preparaBitacora(Cobro.class, sucursal, fecha)
        analisisSucursal(integracionLog)
        .analisisOficinas(integracionLog)
        return integracionLog
    }

    def analisisSucursal(IntegracionLog integracionLog){
        Map res = getRows(integracionLog.sucursal, SQL, integracionLog.fecha, integracionLog.sucursal.id).get(0)
        integracionLog.control = res.control
        integracionLog.registros = res.registros
        return this
    }

    def analisisOficinas(IntegracionLog integracionLog){
        Map res = getLocalRows(integracionLog.sucursal, SQL, [integracionLog.fecha, integracionLog.sucursal.id]).get(0)
        integracionLog.registrosOficinas = res.registros
        integracionLog.controlOficinas = res.control
    }

    def validacionIndividual(Sucursal sucursal, Date fecha ) {
        String sql = "select * from cobro where sucursal_id = ? and date(fecha)=? "
        List params = [sucursal.id, fecha]
        List oficinasRows = getLocalRows(sql, [sucursal.id, fecha])
        List sucursalRows = getRows(sucursal, params)

        def faltantesSucursal = oficinasRows.minus(sucursalRows)
        println faltantesSucursal
        /*
        Sql oficinasDb = getLocalSql()
        def closure = { row ->
            Map found = oficinasDb.firstRow("select * from cobro where id = ?", row.id)
            if (!found) {
                println 'Faltante: ' + row.id
            } else {
                println "Id: ${row.id.substring(0,4)} ${sucursal.nombre} : ${row.importe} Oficinas: ${found.importe}"
            }
        };

        Sql db = getSql(sucursal)
        try{
            db.eachRow("select * from cobro where date(fecha)=? and sucursal_id = ? ", [fecha, sucursal.id],closure)
        }catch (Exception ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)
            println 'Error: ' + msg
        } finally {
            db.close()
            oficinasDb.close()
        }
        */
    }

}
