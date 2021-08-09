package sx.inventario

import sx.core.Sucursal
import sx.core.Venta
import sx.cxc.Cobro
import sx.cxc.NotaDeCredito

class DevolucionDeVenta {

    String	id

    Venta venta

    Cobro cobro

    Long	documento	 = 0

    Date	fecha

    Sucursal sucursal

    BigDecimal	importe	 = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    BigDecimal	importeCortes	 = 0

    String	comentario

    List partidas =[]

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    Date asignado

    Boolean parcial = false

    Date cancelado

    static  hasMany = [partidas:DevolucionDeVentaDet]

    static constraints = {
        comentario nullable: true
        sw2 nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        cobro nullable: true
        fechaInventario nullable: true
        asignado nullable: true
        parcial nullable: true
        cancelado nullable: true
    }

    static  mapping ={
        id generator:'uuid'
        sucursal index: 'SUCURSAL_IDX'
        fecha index: 'FECHA_IDX'
    }

    def findNota() {
        if (cobro) {
            return NotaDeCredito.where{cobro == this.cobro}.find()
        }
    }
}
