package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.core.Sucursal

@ToString(includes = ['sucursal','formaDePago','corte'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = "id")
class CorteCobranza {

	String id

	Sucursal sucursal

	String formaDePago

	BigDecimal deposito = 0.0

  	BigDecimal pagosRegistrados = 0.0

  	BigDecimal cortesAcumulado = 0.0

   	BigDecimal cambiosDeCheques = 0.0

  	Date corte // Fecha y hora del corte

  	Date fecha // Fecha que asigna el usuario 

  	Boolean cierre = false

  	Boolean anticipoCorte = false

  	Date fechaDeposito

  	Boolean cambioCheque  = false

  	String comentario

	Date dateCreated
	
	Date lastUpdated

	String createUser

    String updateUser

    String tipoDeVenta
	

    static constraints = {
    	comentario nullable: true
    	fechaDeposito nullable: true
    	createUser nullable: true
    	updateUser nullable: true
    	tipoDeVenta inList: ['CON', 'COD']
    }

	static mapping = {
		id generator: 'uuid'
		fecha type: 'date'
		fechaDeposito type: 'date'
	}
    
   
}
