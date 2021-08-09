package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includes = 'cliente, comentario, descuento',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='cliente,comentario,descuento')
class PreciosPorCliente {

    String id

    Cliente cliente

    Boolean activo = false

    BigDecimal descuento = 0.00

    String comentario

    Date dateCreated

    Date lastUpdated

    List partidas = []

    String sw2

    static constraints = {
        comentario nullable: true
        sw2 nullable: true
    }

    static hasMany =[partidas:PreciosPorClienteDet]

    static mapping={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
    }

}
