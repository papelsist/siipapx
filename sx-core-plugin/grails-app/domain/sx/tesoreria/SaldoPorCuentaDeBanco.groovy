package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includes = ['ejercicio,mes,cuenta','saldoFinal'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class SaldoPorCuentaDeBanco {

	String id

	CuentaDeBanco cuenta

	BigDecimal saldoInicial

	BigDecimal ingresos = 0.0

	BigDecimal egresos = 0.0

	BigDecimal saldoFinal = 0.0

	Integer ejercicio 

	Integer mes

	Date cierre
	
	Date dateCreated

	Date lastUpdated

    static constraints = {
    	ejercicio inList:(2014..2018)
    	mes inList:(1..12)
    	cuenta unique:['ejercicio','mes']
		cierre(nullable:true)
    }

	static mapping = {
		id generator: 'uuid'
	}


    
}
