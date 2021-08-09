package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = ["id,version,dateCreated, lastUpdated, cuentaPorCobrar"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = 'id')
class Comision {

    String id

    CuentaPorCobrar cxc

    Long documento

    String documentoTipo

    BigDecimal total

    Date fechaDocto

    String cliente

    String sucursal

    Date fechaIni

    Date fechaFin

    Date fechaCobro

    Date pagoComision

    Integer atraso

    BigDecimal pagoComisionable

    BigDecimal comision

    BigDecimal comisionImporte

    String tipo

    String comisionista

    String clave

    Date cancelada

    Boolean revisada = false;

    Boolean enviado = false;


    Date dateCreated
    Date lastUpdated

    Date updateUser
    Date createUser

    static constraints = {
        documentoTipo maxSize: 10
        cancelada nullable: true
        updateUser nullable: true
        createUser nullable: true
        // tipo unique: ['cxc']
        clave maxSize: 5
    }

    static mapping = {
        id generator:'uuid'
        fechaIni type:'date'
        fechaFin type:'date'
        cancelada type: 'date'
        fechaCobro type: 'date'
        pagoComision type: 'date'

    }

}
