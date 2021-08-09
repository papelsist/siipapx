package sx.core

import groovy.transform.ToString


@ToString(excludes = ["dateCreated,lastUpdated"],includeNames=true,includePackage=false)
class VentaCancelada {

	String id

	Venta venta
	
	Date fecha

	String comentario

	String autorizacion
	
	String usuario

    String sw2

	Date dateCreated

	Date lastUpdated

	static constraints = {
        comentario nullable:true
        usuario nullable:true
        sw2 nullable:true

	}

	static mapping = {
        id generator:'uuid'
        fecha type:'date' ,index: 'VENTACANCELADA_IDX1'
    }

}