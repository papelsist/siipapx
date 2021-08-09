package sx.server.integracion

import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component
import sx.security.User
import sx.server.integracion.Importador
import sx.server.integracion.SW2Lookup

@Component
class ImportadorDeUsuarios implements Importador,SW2Lookup  {

    def importar(){

        println "Importando Usuarios"

        def db=dataSourceUser()

        def sql =new Sql(db)

        def query="SELECT 0 as version, false as account_expired,false account_locked,ifnull(apellido_materno,'.') as apellidoMaterno,ifnull(apellido_paterno,'.') as apellidoPaterno,true as enabled, nombres,concat(ifnull(apellido_paterno,''),' ',ifnull(apellido_materno,''),' ',nombres) as nombre," +
                " p.numero_de_trabajador as numeroDeEmpleado,substring(u.clave,1,30) as puesto,o.clave as sucursal,p.numero_de_trabajador as username, '1234' as password" +
                " FROM empleado  e  left join baja_de_empleado b on (b.empleado_id=e.id) join perfil_de_empleado p on ( p.empleado_id=e.id) join puesto  u on (u.id=p.puesto_id) join ubicacion o on (o.id=p.ubicacion_id)" +
                " where (b.id is null or b.fecha<alta) and contratado is true"
        sql.rows(query).each {empleado ->
            println( empleado.nombre)

            def user =User.findByNumeroDeEmpleado(empleado.numeroDeEmpleado)

            if(!user)
                user=new User()


            bindData(user,empleado)

            user.save(failOnError:true, flush:true)
        }

    }


    def importar(empleadoID){

        println "Importando Usuario"+empleadoID

        def db=dataSourceUser()


        def sql =new Sql(db)

        def query="SELECT 0 as version, false as account_expired,false account_locked,ifnull(apellido_materno,'.') as apellidoMaterno,ifnull(apellido_paterno,'.') as apellidoPaterno,true as enabled, nombres,concat(ifnull(apellido_paterno,''),' ',ifnull(apellido_materno,''),' ',nombres) as nombre," +
                " p.numero_de_trabajador as numeroDeEmpleado,substring(u.clave,1,30) as puesto,o.clave as sucursal,p.numero_de_trabajador as username, '1234' as password" +
                " FROM empleado  e  left join baja_de_empleado b on (b.empleado_id=e.id) join perfil_de_empleado p on ( p.empleado_id=e.id) join puesto  u on (u.id=p.puesto_id) join ubicacion o on (o.id=p.ubicacion_id)" +
                " where (b.id is null or b.fecha<alta) and contratado is true and e.id=?"

        def empleado = sql.firstRow(query,[empleadoID])

        println empleado

        def user =User.findByNumeroDeEmpleado(empleado.numeroDeEmpleado)

        if(!user)
            user=new User()

        bindData(user,empleado)

        user.save(failOnError:true, flush:true)

    }


    def dataSourceUser(){

        def driverManagerDs=new DriverManagerDataSource()
        driverManagerDs.driverClassName="com.mysql.jdbc.Driver"
        driverManagerDs.url="jdbc:mysql://10.10.1.229:3306/sx_rh"
        driverManagerDs.username="root"
        driverManagerDs.password="sys"
        return driverManagerDs
    }



}
