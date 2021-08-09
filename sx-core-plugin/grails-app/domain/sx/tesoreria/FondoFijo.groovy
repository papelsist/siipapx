package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.core.Sucursal

@ToString(includes = ['sucursal,documento','importe'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = "id")
class FondoFijo {

	String id

    Sucursal sucursal

    Date fecha // Fecha que asigna el usuario

	String documento

    String descripcion

    Boolean rembolso = false

	BigDecimal importe = 0.0

    String solicitud

    Date solicitado

    String comentario

    FondoFijo fondo
  	
	Date dateCreated
	
	Date lastUpdated

	String createUser

    String updateUser


    static constraints = {
        documento nullable: true
        descripcion nullable: true
        solicitud nullable: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true
        fondo nullable: true
        solicitado nullable: true
    }

	static mapping = {
		id generator: 'uuid'
        solicitado type: 'date'
	}
   
}
