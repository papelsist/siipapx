package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.tesoreria.Banco
import sx.tesoreria.Ficha

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CobroCheque {

    String id

    Cobro cobro

    Banco bancoOrigen

    String numeroDeCuenta

    String nombre

    String emisor

    Long numero = 0

    Boolean postFechado = false

    Boolean cambioPorEfectivo = false

    Date vencimiento

    Ficha ficha

    String sw2

    Date dateCreated

    Date lastUpdated


    static belongsTo = [cobro: Cobro]

    static constraints = {
        emisor nullable:true
        cobro nullable: true
        vencimiento nullable:true
        ficha nullable:true
        postFechado nullable: true
        numero nullable: true
        nombre nullable: true
        sw2 nullable: true

    }

    static mapping={

        id generator:'uuid'
        vencimiento type: 'date'
    }

    
}
