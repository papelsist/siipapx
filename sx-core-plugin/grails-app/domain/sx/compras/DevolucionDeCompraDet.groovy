package sx.compras

import sx.core.Inventario
import sx.core.Producto

class DevolucionDeCompraDet {


    String	id

    DevolucionDeCompra devolucionDeCompra


    Inventario inventario

    Producto producto

    BigDecimal	cantidad	 = 0

    BigDecimal	costoDec	 = 0

    BigDecimal	importeCosto	 = 0

    String	comentario

    String sw2

    RecepcionDeCompraDet recepcionDeCompraDet

    Date dateCreated

    Date lastUpdated

    static belongsTo = [ devolucionDeCompra:DevolucionDeCompra ]

    static constraints = {
        comentario nullable: true
        inventario nullable: true
        sw2 nullable: true
        recepcionDeCompraDet nullable: true

    }

    static mapping = {
        id generator:'uuid'

    }

}
