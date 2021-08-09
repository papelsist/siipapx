package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.tesoreria.CorteDeTarjeta

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CobroTarjeta {

    String	id

    Cobro	cobro

    Boolean	debitoCredito = true

    Boolean	visaMaster = true

    BigDecimal comision = 0

    String sw2

    CorteDeTarjeta corteDeTarjeta

    String corte

    Integer	validacion = 0

    Date dateCreated

    Date lastUpdated

    static belongsTo=[cobro:Cobro]

    static mapping = {
        id generator : 'uuid'
    }

    static constraints = {
        corteDeTarjeta nullable: true
        corte nullable: true
        cobro nullable: true
        sw2 nullable: true
    }
}
