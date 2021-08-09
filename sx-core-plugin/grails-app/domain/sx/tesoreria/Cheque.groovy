package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(includes = ['cuenta,folio'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id,folio'])
class Cheque {

	String id

	Integer folio

	Date impresion

	MovimientoDeCuenta egreso
	
	Date cancelacion

	String comentarioCancelacion

	Date fechaDevolucion 

	MovimientoDeCuenta devolucion

	Date dateCreated
	
	Date lastUpdated
	

    static constraints = {
		impresion(nullable:true)
		cancelacion nullable:true
		comentarioCancelacion nullable:true
		folio min:1
		fechaDevolucion nullable:true
		devolucion nullable: true
    }

	static mapping = {
		id generator: 'uuid'
	}
    
   
}
