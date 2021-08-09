package sx.crm

import com.luxsoft.utils.MonedaUtils
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.transaction.NotTransactional
import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import sx.core.Cliente

import javax.sql.DataSource
import java.sql.SQLException

// @Transactional
// @GrailsCompileStatic
class BonificacionMCService {

    DataSource dataSource

    String SQL ="""
            SELECT 
        X.cliente_id as clienteId
        ,X.nombre
        ,(X.neto) as ventas
        ,(X.KILOS) as ventasKilos
        ,x.ult_vta as ultimaVenta
        ,x.facs as facturas
        ,x.suc as sucursal
        FROM (
        select a.cliente_id 
        ,(SELECT x.clave FROM cliente x where x.ID=a.CLIENTE_ID) as clave
        ,v.nombre
        ,sum(a.subtotal) as neto
        ,sum(v.kilos) as kilos
        ,count(*) as facs
        ,max(A.fecha) as ult_vta
        ,A.tipo
        ,(SELECT s.nombre FROM sucursal s where s.ID=a.sucursal_id) as suc 
        from cuenta_por_cobrar a  join venta v on(v.cuenta_por_cobrar_id=a.id)
        where YEAR(a.fecha) = ? and MONTH(a.fecha) = ? AND A.TIPO IN('CON','COD')
        and a.cliente_id not in('402880fc5e4ec411015e4ec8fbbb045c','402880fc5e4ec411015e4ec9349204ae','402880fc5e4ec411015e4ecc5dfc0554')
        group by A.CLIENTE_ID
        order by a.cliente_id,a.fecha desc
        ) AS X
        group by X.cliente_id
        order by sum(X.neto) desc
        limit 200 

    """
    @NotTransactional
    def generar(Integer ejercicio, Integer mes) {
        log.info('Generando bonificaciones para  {} - {}', mes, ejercicio)
        def found = BonificacionMC.where{ ejercicio == ejercicio && mes == mes}.find()
        if(!found) {
            def rows = getRows(SQL, [ejercicio, mes])
            Date fecha = new Date()
            int vigencia = 90
            Date vto = fecha + vigencia
            int posicion = 1

            rows.each { row ->
                BonificacionMC bono = new BonificacionMC()
                Cliente cliente = Cliente.get(row.clienteId)
                bono.cliente = cliente
                bono.nombre = cliente.nombre
                bono.ejercicio = ejercicio
                bono.mes = mes
                bono.fecha = fecha
                bono.vigenciaDias = vigencia
                bono.properties = row
                bono.bono = 0.01
                bono.posicion = posicion
                BigDecimal importe = bono.ventas * bono.bono
                bono.importe = MonedaUtils.round(importe, 0)
                bono.save failOnError:true, flush:true
                posicion++
            }

        } else {
            log.info('Bonificaciones ya generadas para el periodo {} / {}' , ejercicio, mes)

        }

    }

    BonificacionMC autorizar(BonificacionMC bonificacionMC) {
        if(bonificacionMC.suspendido)
            return bonificacionMC
        Date fecha = new Date()
        Date vto = fecha + bonificacionMC.vigenciaDias
        bonificacionMC.autorizado = fecha
        bonificacionMC.vencimiento = vto
        bonificacionMC.save flush: true
        return bonificacionMC
    }

    BonificacionMC suspender(BonificacionMC bonificacionMC, String comentario) {
        bonificacionMC.suspendido = new Date()
        bonificacionMC.suspendidoComentario = comentario
        bonificacionMC.ajuste = bonificacionMC.getDisponible()
        bonificacionMC.save flush: true
        return bonificacionMC
    }

    List getRows(String sql, List params) {
        def db = getLocalSql()
        try {
            return db.rows(sql, params)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    Sql getLocalSql(){
        Sql sql = new Sql(this.dataSource)
        return sql;
    }
}
