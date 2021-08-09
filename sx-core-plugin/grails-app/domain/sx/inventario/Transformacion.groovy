package sx.inventario

import sx.core.Autorizacion
import sx.core.Sucursal
import sx.core.Venta

class Transformacion {

    String	id

    Sucursal	sucursal

    Autorizacion	autorizacion

    String	tipo

    Long	documento	 = 0

    Date	fecha

    Venta	venta

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

    static  hasMany = [partidas:TransformacionDet]


    static constraints = {
        autorizacion nullable: true
        tipo nullable: true
        venta nullable: true
        comentario nullable: true
        sw2 nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        porInventario nullable: true
        fechaInventario nullable: true
        cancelado nullable: true

    }


    static mapping = {
        id generator:'uuid'
        fecha index: 'FECHA_IDX'
        sucursal index: 'SUCURSAL_IDX'
    }


}
