package sx.inventario

import sx.core.Producto

class ConteoDet {

    String	id

    Long	documento= 0

    Producto producto

    Conteo	conteo

    BigDecimal	cantidad= 0

    Long	indice	 = 0

    static  belongsTo = [conteo:Conteo]

    static constraints = {
    }

    static mapping= {
        id generator: 'uuid'
    }

}
