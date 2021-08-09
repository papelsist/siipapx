package sx.core

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes='id')
class Socio {

	String id

    String clave="NX"

    String nombre

    BigDecimal comisionCobrador = 0.0

    BigDecimal comisionVendedor = 0.0

    Vendedor vendedor

    Cliente cliente

    String direccion

    Direccion direccionFiscal

    Long sw2

    static constraints = {
        vendedor nullable:true
        sw2 nullable:true
        direccionFiscal nullable: true
    }

    static mapping = {
        id generator:'uuid'
    }

    static embedded = ['direccionFiscal']

    String toString() {
    	return nombre
    }
}