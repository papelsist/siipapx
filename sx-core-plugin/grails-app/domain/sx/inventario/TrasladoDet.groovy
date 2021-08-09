package sx.inventario

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Inventario
import sx.core.Producto

@GrailsCompileStatic
@ToString(includes = 'producto,cantidad', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id, producto, cantidad')
class TrasladoDet {

    String	id

    Traslado	traslado

    Inventario inventario

    Producto producto

    BigDecimal	solicitado	 = 0

    BigDecimal	cantidad	 = 0

    Long	cortes	 = 0

    BigDecimal  kilos     = 0

    String	cortesInstruccion

    String	comentario

    String sw2

    Date dateCreated

    Date lastUpdated

    static belongsTo = [traslado:Traslado]

    static constraints = {
        cortesInstruccion nullable:true
        comentario nullable: true
        sw2 nullable: true
        inventario nullable: true
    }

    static mapping = {
        id generator: 'uuid'
        producto index:'PRODUCTO_IDX'
    }
}
