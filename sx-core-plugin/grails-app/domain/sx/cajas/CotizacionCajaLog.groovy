package sx.cajas

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.core.Producto

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal')

class CotizacionCajaLog {

    String	id

    String sucursal

    Producto producto

    Date fecha = new Date()

    Long folio

    Long piezas = 0

    BigDecimal precioPiezaContado = 0.00

    BigDecimal precioPiezaCredito = 0.00

    BigDecimal precioEspecialCredito = 0.00

    BigDecimal precioEspecialContado = 0.00

    static constraints = {
        folio nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type: 'date'
    }

}
