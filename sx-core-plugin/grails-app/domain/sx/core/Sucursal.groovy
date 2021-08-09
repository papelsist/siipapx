package sx.core

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes='nombre',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='nombre,clave')
class Sucursal {

	String id

	Boolean activa= true

	String nombre

	String clave

    Boolean almacen = true

    Direccion direccion

	Long sw2

    String dbUrl

	Date dateCreated

	Date lastUpdated


    static constraints = {
        clave minSize:1, maxSize:20, unique:true
        nombre unique:true
        sw2 nullable:true
        dbUrl nullble: true
    }

    static mapping = {
    	id generator:'uuid'
    }

    static embedded = ['direccion']
}
