package sx.inventario

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Producto

@ToString(includes = 'cantidad,indice,producto', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id, indice,cantidad, producto, sector, comentario')
class SectorDet {

    String id

    Producto producto

    Sector	sector

    BigDecimal	cantidad	 = 0

    String	comentario

    BigDecimal	indice	 = 0.0


    static  belongsTo = [sector:Sector]


    static constraints = {
        comentario nullable: true
        indice(scale:2)
    }


    static mapping = {
        id generator:'uuid'
    }
}
