package sx.inventario

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Inventario
import sx.core.Producto
import sx.core.VentaDet

@ToString(includes = ["id,cantidad"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id')
class DevolucionDeVentaDet {


    String	id

    VentaDet ventaDet

    DevolucionDeVenta devolucionDeVenta

    Inventario inventario

    Producto producto

    BigDecimal	cantidad	 = 0

    BigDecimal	costoDev	 = 0

    BigDecimal	importeCosto	 = 0

    String	comentario

    Date dateCreated

    Date lastUpdated

    String sw2



    static  belongsTo = [devolucionDeVenta:DevolucionDeVenta]

    static constraints = {
        sw2 nullable:true
        comentario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        inventario nullable: true

    }

    static  mapping ={
        id generator:'uuid'
        producto index: 'PRODUCTO_IDX'
    }

}
