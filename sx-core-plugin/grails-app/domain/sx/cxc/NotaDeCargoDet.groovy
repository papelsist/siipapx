package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Venta

/**
 * Created by rcancino on 23/03/17.
 */

@ToString(excludes = ["id, documento, documentoTipo"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','cuentaPorCobrar'])
class NotaDeCargoDet {

    String id

    String concepto

    BigDecimal cargo = 0.0

    BigDecimal importe = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal total = 0.0

    CuentaPorCobrar	cuentaPorCobrar

    Long documento = 0

    String documentoTipo

    Date documentoFecha

    BigDecimal documentoTotal = 0.0

    BigDecimal documentoSaldo = 0.0

    String sucursal

    String comentario

    static constraints = {
        comentario nullable:true
        cuentaPorCobrar nullable: true
        concepto nullable: true
        documentoTipo maxSize:10
        sucursal maxSize: 20
    }

    static mapping={
        id generator:'uuid'
        documentoFecha type: 'date'
    }

    static belongsTo =[nota:NotaDeCargo]
}
