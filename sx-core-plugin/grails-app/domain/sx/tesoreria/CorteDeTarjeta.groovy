package sx.tesoreria

import sx.core.Sucursal
import sx.cxc.CobroTarjeta
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

class CorteDeTarjeta {

	String id

	Long folio = 0

	Sucursal sucursal

	Date corte

	CuentaDeBanco cuentaDeBanco

	BigDecimal total = 0.0

	String comentario

	Boolean	visaMaster	 = true

	String sw2

	List partidas = []

	List aplicaciones = []

	Date dateCreated

	Date lastUpdated

    String estatus

	static hasMany =[partidas: CobroTarjeta, aplicaciones: CorteDeTarjetaAplicacion]

    static transients = ['estatus']

    static constraints = {
    	sw2 nullable: true
    	comentario nullable: true
    }

    static mapping ={
		id generator: 'uuid'
        corte type: 'date'
        aplicaciones cascade: "all-delete-orphan"
    }

    def getEstatus() {
        def found = this.aplicaciones.find{ CorteDeTarjetaAplicacion it -> it.ingreso}
        return found ? 'APLICADO' : 'PENDIENTE'
    }
}





