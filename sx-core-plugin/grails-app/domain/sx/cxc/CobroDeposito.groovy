package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CobroDeposito {

    String id

    Banco bancoOrigen

    CuentaDeBanco cuentaDestino

    Boolean conciliado = false

    Long folio = 0

    Date fechaDeposito
    
    BigDecimal totalCheque = 0.0

    BigDecimal totalEfectivo = 0.0

    BigDecimal totalTarjeta = 0.0

    MovimientoDeCuenta ingreso

    String sw2

    Date dateCreated

    Date lastUpdated


    static belongsTo = [cobro: Cobro]

    static constraints = {
        ingreso nullable:true
        sw2 nullable: true
    }

    static mapping={
        id generator:'uuid'
        fechaDeposito type:'date' ,index: 'COBRO_TRANS_IDX1'

    }
}
