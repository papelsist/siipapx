package sx.integracion

import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import sx.core.Sucursal

import java.sql.SQLException

trait Integracion {

    @Autowired
    @Qualifier('dataSource')
    def dataSource


    @Autowired
    @Qualifier('grailsApplication')
    def grailsApplication

    IntegracionLog preparaBitacora(Class entidad, Sucursal sucursal, Date fecha) {
        IntegracionLog integracionLog = new IntegracionLog()
        integracionLog.sucursal = sucursal
        integracionLog.sucursalNombre = sucursal.nombre
        integracionLog.fecha = fecha
        integracionLog.entidad = entidad.class.simpleName
        return integracionLog
    }

    def getRows(Sucursal sucursal, String sql, ...params) {
        println "Rows con ${sucursal} Params: ${params} SQL: ${sql}"
        def db = getSql(sucursal)
        try {
            return db.rows(sql,params)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    def getSql(Sucursal sucursal) {
        String user = 'root'
        String password = 'sys'
        String driver = 'com.mysql.jdbc.Driver'
        Sql db = Sql.newInstance(sucursal.dbUrl, user, password, driver)
        return db
    }

    def getLocalSql(){
        Sql sql = new Sql(this.dataSource)
        return sql;
    }

    def getLocalRows(String sql, List params) {
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

    def getLocalRow(String sql, List params){
        Sql db = getLocalSql()
        return db.firstRow(sql, params)
    }

    def tempo() {
        Sql sql = getLocalSql()
        sql.firstRow("select count(*) from Cfdi where uuid is not null")
    }

}