package sx.compras

import sx.core.Proveedor
import sx.core.Sucursal

class DevolucionDeCompra {


    String	id

    Sucursal sucursal

    Proveedor proveedor

    String notaCxp_id

    Long	documento = 0

    Date	fecha

    String	referencia

    Date	fechaReferencia

    String	comentario

    List partidas = []

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    RecepcionDeCompra recepcionDeCompra

    static hasMany = [partidas:DevolucionDeCompraDet]

    static constraints = {

        notaCxp_id nullable: true
        referencia nullable: true
        fechaReferencia nullable: true
        comentario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        sw2 nullable: true
        recepcionDeCompra nullable: true
        fechaInventario nullable: true
    }

    static  mapping ={
        id generator: 'uuid'
        partidas cascade: "all-delete-orphan"
        sucursal index: 'SUCURSAL_IDX'
        proveedor index: 'PROVEEDOR_ID'
        fecha type: 'date', index: 'FECHA_IDX'
    }
}
