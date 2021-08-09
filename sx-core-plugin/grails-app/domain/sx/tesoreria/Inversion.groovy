package sx.tesoreria




class Inversion extends Traspaso{

	String id
	
	Date rendimientoFecha = new Date()

	BigDecimal rendimientoReal = 0.0

	BigDecimal rendimientoCalculado = 0.0

	BigDecimal rendimientoImpuesto = 0.0
	
	BigDecimal tasa = 16.00

	BigDecimal tasaIsr = 0.0

	BigDecimal importeIsr=0.0
	
	int plazo = 1
	
	Date vencimiento= new Date()
	

    static constraints = {
		rendimientoFecha(nullable:true)
		vencimiento(validator:{val,obj ->
			if(val<obj.fecha)
				return "vencimientoInvalido" 
		})
    }
	
	static mapping ={
		id generator: 'uuid'
        rendimientoFecha type:'date' 
    }
	
	
}
