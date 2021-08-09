package sx.server.integracion.exportacion

import groovy.sql.Sql
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by rcancino on 03/05/17.
 */
trait Exportador {

    @Autowired
    @Qualifier('dataSource_importacion')
    def dataSource


    Logger logger = LoggerFactory.getLogger(getClass());


    def toSqlDate(Date date){
        return date.format('yyyy-MM-dd')
    }

    Sql buildSql(){
        return new Sql(dataSource)
    }

}