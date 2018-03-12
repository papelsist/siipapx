package sx.tesoreria

import sx.core.Sucursal
import sx.cxc.CobroTarjeta
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

class CorteDeTarjeta {

	String id

	Long folio = 0

	Sucursal sucursal

	Date corte = new Date()

	CuentaDeBanco cuentaDeBanco

	BigDecimal total = 0.0

	String comentario

	Boolean	visaMaster	 = true

	String sw2

	List partidas = []

	List aplicaciones = []

	Date dateCreated

	Date lastUpdated

	static hasMany =[partidas: CobroTarjeta, aplicaciones: CorteDeTarjetaAplicacion]

    static constraints = {
    	sw2 nullable: true
    	comentario nullable: true
    	corte nullable: true
    }

    static mapping ={
		id generator: 'uuid'
        corte type: 'date'
        aplicaciones cascade: "all-delete-orphan"
    }
}





