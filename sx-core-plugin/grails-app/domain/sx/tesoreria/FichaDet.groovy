package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.cxc.Cobro

@ToString(includes = ['banco,cheque,efectivo'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class FichaDet {

	String id
	
	BigDecimal cheque = 0.0
	
	BigDecimal efectivo = 0.0
	
	String banco
	
	Cobro cobro

	String sw2 

	Date dateCreated
	Date lastUpdated

	// static belongsTo = [ficha: Ficha]

    static constraints = {
    	sw2 nullable:true
    	banco maxSize: 50
    }

	static mapping = {
		id generator: 'uuid'
	}

}
