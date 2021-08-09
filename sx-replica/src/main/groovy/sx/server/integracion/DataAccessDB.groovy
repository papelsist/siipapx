package sx.server.integracion

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

@Component
class DataAccesDB{


    @Autowired
    @Qualifier('dataSource')
    def dataSource

def dataSourceResolve(ip, bd, user, password){

    def urlJdbc='jdbc:mysql://'+ip+"/"+bd
    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def dataSourceResolveUrl(urlJdbc, user, password){

    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def getSql(dataSource){
    def sql=new Sql(dataSource)
    return sql
}

def getSucursal(sucName){
def sucursal=Sucursal.findByNombre(sucName) 
return sucursal
}

def getConfig(entity){

    def sql = getSql(dataSource)

    def config = sql.rows("Select * from entity_configuration where name=?",[entity])
    return config
}




}