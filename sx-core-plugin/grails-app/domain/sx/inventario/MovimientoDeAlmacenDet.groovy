package sx.inventario

import sx.core.Inventario
import sx.core.Producto

class MovimientoDeAlmacenDet {

    String	id

    Inventario inventario

    Producto producto

    MovimientoDeAlmacen movimientoDeAlmacen

    BigDecimal	cantidad	 = 0

    String	tipoCIS

    String	comentario

    String sw2

    Date dateCreated

    Date lastUpdated



    static belongsTo = [movimientoDeAlmacen:MovimientoDeAlmacen]


    static constraints = {
        sw2 nullable: true
        comentario nullable: true
        tipoCIS nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        inventario nullable: true

    }


    static mapping={
        id generator:'uuid'
        producto index:'PRODUCTO_IDX'

    }
}
