package sx.inventario

import sx.core.Sucursal

class MovimientoDeAlmacen {

    String	id

    Sucursal sucursal

    Long	documento	 = 0

    Date	fecha

    String	tipo

    Boolean	porInventario	 = false

    String	comentario

    List partidas =[]

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    Date cancelado

    static hasMany = [partidas:MovimientoDeAlmacenDet]

    static constraints = {
        sw2 nullable: true
        comentario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        fechaInventario nullable: true
        cancelado nullable: true
    }

    static mapping = {
        id generator:'uuid'
        sucursal index:'SUCURSAL_IDX'
        fecha index:'FECHA_IDX'

    }

}
