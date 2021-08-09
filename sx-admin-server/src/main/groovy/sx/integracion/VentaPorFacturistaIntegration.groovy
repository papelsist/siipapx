package sx.integracion

import com.luxsoft.utils.Periodo
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.stereotype.Component
import sx.bi.VentaPorFacturista
import sx.core.Sucursal

import java.sql.SQLException


@Component
@Slf4j
class VentaPorFacturistaIntegration implements Integracion{

    static String SQL_COMMAND = """
        SELECT fecha,sucursal_id,sucursalNom, create_user as createUser,facturista
        ,sum(con) con,sum(cod) cod,sum(cre) as cre,sum(canc) canc,sum(devs) devs,sum(facs) facs,sum(importe) importe,sum(partidas) partidas,sum(ped_fact) pedFact
        ,sum(ped) ped,sum(pedMosCON) pedMosCON,sum(pedMosCOD) pedMosCOD,sum(pedMosCRE) pedMosCRE,sum(pedTelCON) pedTelCON,sum(pedTelCOD) pedTelCOD,sum(pedTelCRE) pedTelCRE
        FROM (
        SELECT 'PED' as tipo,fecha,sucursal_id,(SELECT s.nombre FROM sucursal s WHERE v.sucursal_id=s.id) as sucursalNom,create_user
        ,(SELECT u.nombre FROM user u where v.create_user=u.username) as facturista
        ,0 con,0 cod,0 cre,0 canc,0 devs,0 facs,0 importe,0 partidas
        ,SUM(case when v.facturar is not null and v.facturar_usuario <> v.update_user then 1 else 0 end) as ped_fact
        ,count(*) as ped
        ,SUM(case when v.tipo = 'CON' and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCON
        ,SUM(case when v.tipo = 'CON' and v.cod is true and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCOD
        ,SUM(case when v.tipo = 'CRE' and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCRE
        ,SUM(case when v.tipo = 'CON' and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCON
        ,SUM(case when v.tipo = 'CON' and v.cod is true and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCOD
        ,SUM(case when v.tipo = 'CRE' and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCRE
         FROM VENTA V WHERE FECHA='%FECHA%'
         GROUP BY fecha,sucursal_id,create_user
        union
        SELECT 'FAC' as tipo,fecha,sucursal_id,(SELECT s.nombre FROM sucursal s WHERE v.sucursal_id=s.id) as sucursalNom,update_user
        ,(SELECT u.nombre FROM user u where v.update_user=u.username) as facturista
        ,SUM(case when v.tipo = 'CON' then 1 else 0 end) as con
        ,SUM(case when v.tipo = 'COD' then 1 else 0 end) as cod
        ,SUM(case when v.tipo = 'CRE' then 1 else 0 end) as cre
        ,SUM(case when v.cancelada is not null then 1 else 0 end) as canc
        ,(SELECT count(*) FROM devolucion_de_venta d join venta x on(d.venta_id=x.id) where d.parcial is false and x.cuenta_por_cobrar_id is not null and x.update_user = v.update_user and date(d.fecha)='%FECHA%') as devs
        ,count(*) as facs,sum(subtotal * tipo_de_cambio) as importe
        ,(SELECT count(*) FROM venta_det d join venta x on(d.venta_id=x.id) join cuenta_por_cobrar z on(x.cuenta_por_cobrar_id=z.id) where z.cfdi_id is not null and z.cancelada is null and x.update_user = v.update_user and date(z.fecha)='%FECHA%') as partidas
        ,0 as ped_fact,0 ped,0 pedMosCON,0 pedMosCOD,0 pedMosCRE,0 pedTelCON,0 pedTelCOD,0 pedTelCRE
        FROM cuenta_por_cobrar V WHERE cfdi_id is not null and sw2 is null and FECHA='%FECHA%'
        GROUP BY fecha,sucursal_id,update_user
        ) AS A
        GROUP BY
        fecha,sucursal_id,create_user
    """

    String getCommand(Date fecha) {
        return SQL_COMMAND.replaceAll("%FECHA%", fecha.format('yyyy-MM-dd'))
    }

    def readVentas(Sucursal sucursal, Date fecha) {
        Sql db = getSql(sucursal)
        try {
            return db.rows(getCommand(fecha))
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    /**
     * Actualizar los dias que van del mes exceptuando la fecha actual
     *
     * @return
     */
    def actualizarMTD() {
        Date fechaInicial = Periodo.getCurrentMonth().fechaInicial
        Date fechaFinal = new Date() - 1
        (fechaInicial..fechaFinal).each {
            actualizar(it)
        }
    }

    def actualizar(Date fecha, boolean prune = true) {
        log.info("Actualizando ventas por facturista ${fecha.format('dd/MM/yyyy')}")
        if(prune) {
            deleteRecords(fecha)
        }
        List<Sucursal> sucursales = Sucursal.where{activa == true && dbUrl!= null && nombre!= 'OFICINAS'}.list()
        sucursales.each { suc ->
            actualizar(suc, fecha)
        }
        log.info("Ventas por facturista actualizadas para el ${fecha.format('dd/MM/yyyy')}")
    }

    def actualizar(Sucursal sucursal, Date fecha) {
        try {

            def rows = readVentas(sucursal, fecha)
            rows.each { row ->
                VentaPorFacturista vta = new VentaPorFacturista()
                vta.properties = row
                vta.sucursal = sucursal
                vta.save failOnError: true, flush: true
            }
        }catch (Exception e){
            def message = ExceptionUtils.getRootCauseMessage(e)
            log.error('Error integrando ventas por facturista de la sucursal {} Msg: {}', sucursal.nombre, message)
        }
    }

    def deleteRecords(Date fecha) {
        def res = VentaPorFacturista.executeUpdate("delete VentaPorFacturista v where date(v.fecha)=?",[fecha])
        log.info('{} registros eliminando para {}', res, fecha.format('dd/MM/yyyy'))
    }

    def insertVentas(List rows) {
        /*
        Sql db = getLocalSql()
        try {
            def table = db.dataSet("venta_por_facturista")
            rows.each {
                table.add()
            }
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
        */
    }
}
