package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cobrador
import sx.core.Socio


// @ToString(excludes = ["id,version,dateCreated, lastUpdated, cuentaPorCobrar"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = 'id')
class VentaCredito {

    String id

    Integer plazo = 0

    Boolean vencimientoFactura = true

    Date vencimiento

	Date fechaRecepcionCxc

    Integer diaRevision = 0

    Date fechaRevision

    Date fechaRevisionCxc

    BigDecimal	descuento	 = 0

    Boolean revision = true

    Boolean revisada = false

    Integer diaPago = 0

    Date	fechaPago

    Date reprogramarPago

    String comentarioReprogramarPago

    Cobrador cobrador

    Socio socio

    Integer operador = 1

    String	sw2

    String	comentario

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    CuentaPorCobrar cuentaPorCobrar;

    static constraints = {
        comentarioReprogramarPago nullable:true
        fechaRecepcionCxc nullable:true
        reprogramarPago nullable:true
        socio nullable:true
        comentarioReprogramarPago nullable:  true
        comentario nullable: true
        sw2 nullable: true
        updateUser nullable: true
        createUser nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fechaRevision type:'date', index: 'VENTACRE_IDX1'
        fechaRevisionCxc type:'date', index: 'VENTACRE_IDX1'
        fechaRecepcionCxc type:'date' ,index: 'VENTACRE_IDX1'
        vencimiento type: 'date', index: 'VENTACRE_IDX2'
        fechaPago type:'date', index: 'VENTACRE_IDX2'
        reprogramarPago type:'date'

    }

    static belongsTo = [cuentaPorCobrar: CuentaPorCobrar];
}