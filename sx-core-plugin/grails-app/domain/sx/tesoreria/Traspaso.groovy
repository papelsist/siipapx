package sx.tesoreria

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

import sx.utils.MonedaUtils

@EqualsAndHashCode(includes='id')
@ToString(includeNames=true,includePackage=false)
class Traspaso {

	String id
	
	Date fecha = new Date()

	CuentaDeBanco cuentaOrigen

	CuentaDeBanco cuentaDestino

	Currency moneda = MonedaUtils.PESOS

	BigDecimal importe = 0.0

	BigDecimal comision = 0.0

	BigDecimal impuesto = 0.0

	String comentario
	
	Date dateCreated

	Date lastUpdated
	
	static hasMany = [movimientos:MovimientoDeCuenta]

    static constraints = {
		cuentaDestino validator:{val, obj ->
			if(obj.cuentaOrigen==val)
				return "mismaCuentaError"
			if(obj.cuentaOrigen.moneda!=val.moneda)
				return "diferenteMonedaError"
			
		}
		comentario(blank:true)
    }
	
	static mapping ={
		id generator: 'uuid'
		fecha type: 'date'
		movimientos cascad:"all-delete-orphan"
	}
	
	
}
