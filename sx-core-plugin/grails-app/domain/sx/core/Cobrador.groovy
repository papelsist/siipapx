package sx.core

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes='id')
class Cobrador {

    String id

    Long sw2

    Boolean activo

    String clave

    String apellidoPaterno

    String apellidoMaterno

    String nombres

    String curp

    String rfc

    BigDecimal comision=0

    Date dateCreated

    Date lastUpdated

    static constraints = {
        sw2 nullable:true
        clave nullable:true
        apellidoMaterno nullable:true
        apellidoPaterno nullable:true
        nombres nullable:true
        curp nullable:true
        rfc nullable:true
        comision nullable:true
        dateCreated nullable:true
        lastUpdated nullable:true
    }

    static mapping ={
        id generator:'uuid'
    }
    String toString() {
        return "$nombres $apellidoPaterno"
    }
}


