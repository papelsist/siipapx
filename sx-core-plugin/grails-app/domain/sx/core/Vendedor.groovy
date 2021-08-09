package sx.core

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes='id')
class Vendedor {

    String id

    Long sw2

    Boolean activo

    String apellidoPaterno

    String apellidoMaterno

    String nombres

    String curp

    String rfc

    BigDecimal comisionContado=0

    BigDecimal comisionCredito=0

    Date dateCreated

    Date lastUpdated

    static constraints = {
        sw2 nullable:true
        activo nullable:true
        apellidoPaterno nullable:true
        apellidoMaterno nullable:true
        nombres nullable:true
        curp nullable:true
        rfc nullable:true
    }

    static mapping={
        id generator:'uuid'
    }

    String toString() {
        return "$nombres $apellidoPaterno"
    }
}
