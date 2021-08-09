package sx.server.integracion

import grails.web.databinding.DataBinder
import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier


import java.sql.SQLException

/**
 * Created by rcancino on 16/08/16.
 */
trait Importador extends DataBinder{

    @Autowired
    @Qualifier('dataSource_importacion')
    def dataSource

    Logger logger = LoggerFactory.getLogger(getClass());

    def importarPorRegistros(String sql, List params,Closure closure){
        Sql db = getSql()
        try {
             db.eachRow(sql,params,closure)
        }catch (SQLException e){
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            logger.error(message,c)
            throw new RuntimeException(message,c)
        }finally {
           db.close()
        }
    }

    def List leerRegistros(String sql,List params){
        Sql db = getSql()
        try {
            return db.rows(sql,params)
        }catch (SQLException e){
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            logger.error(message,c)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    def  findRegistro(String sql,List params){
        Sql db = getSql()
        try {
            return db.firstRow(sql,params)
        }catch (SQLException e){
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            logger.error(message,c)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    def toSqlDate(Date date){
        return date.format('yyyy-MM-dd')
    }

    Sql getSql(){
       return new Sql(dataSource)
    }
    

}