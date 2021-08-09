package sx.logistica

import sx.core.Sucursal

class ChoferUbicacion {

    String	id

    Chofer	chofer

    Embarque	embarque

    Sucursal sucursal

    Date	fecha

    BigDecimal	latitud	 = 0

    BigDecimal	longitud	 = 0

    String	incidencia	 = 0


    static constraints = {
        embarque nullable: true
        latitud nullable: true
        longitud nullable: true
        incidencia nullable: true

    }

    static mapping ={
        id generator:'uuid'
    }
}
