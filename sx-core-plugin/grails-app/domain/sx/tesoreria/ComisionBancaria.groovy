package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes='id')
@ToString( excludes = "version, lastUpdated, dateCreated, cxc",includeNames=true,includePackage=false)
class ComisionBancaria {

    String id

    Date fecha

    CuentaDeBanco cuenta

    BigDecimal comision = 0.0

    BigDecimal impuestoTasa = 0.0

    BigDecimal impuesto = 0.0

    String comentario

    String referenciaBancaria

    String cxp

    Set<MovimientoDeCuenta> movimientos

    Date dateCreated
    Date lastUpdated

    static hasMany =[movimientos:MovimientoDeCuenta]

    static constraints = {
        comentario(nullable:true,maxSize:200)
        referenciaBancaria(nullable:true,maxSize:100)
        cxp nullable:true
    }

    static mapping ={
        id generator: 'uuid'
        fecha type:'date'
        movimientos cascade:"all-delete-orphan"
    }

}

