package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CobroTransferencia {

    String id

    Banco bancoOrigen

    CuentaDeBanco cuentaDestino

    Boolean conciliado = false

    Long folio = 0

    Date fechaDeposito

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
