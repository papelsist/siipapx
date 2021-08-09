package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Sucursal

@ToString(excludes = ["id,version,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class AplicacionDeCobro {

    String id

    Cobro cobro

    CuentaPorCobrar	cuentaPorCobrar

    Date fecha

    BigDecimal importe = 0

    String formaDePago

    String	sw2

    String recibo

    String moneda = 'MXN'

    BigDecimal tipoDeCambio = 1.0 // 22.4413

    Date dateCreated
    Date lastUpdated
    String createUser
    String updateUser

    static constraints = {
        sw2 nullable: true
        cobro nullable: true
        createUser nullable: true
        updateUser nullable: true
        formaDePago nullable: true
        recibo nullable: true
        moneda nullable: true, maxSize: 3
        tipoDeCambio nullable: true, scale: 6
    }

    static mapping = {
        id generator:'uuid'
        fecha type: 'date'
    }

    static belongsTo = [cobro: Cobro]

}


