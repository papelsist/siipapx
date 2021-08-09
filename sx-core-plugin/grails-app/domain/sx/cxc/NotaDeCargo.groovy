package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

/**
 * Created by rcancino on 23/03/17.
 */
@ToString( includes = "serie, folio, fecha, cliente", includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','serie', 'folio'])
class NotaDeCargo {

    String	id

    Sucursal sucursal

    Cliente	cliente

    Date fecha = new Date()

    String tipo

    String serie

    Long folio

    String formaDePago = "POR DEFINIR"

    BigDecimal importe

    BigDecimal impuesto

    BigDecimal total

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tipoDeCambio = 1.0

    String comentario

    CuentaPorCobrar cuentaPorCobrar

    Cfdi cfdi

    String usoDeCfdi

    List partidas = []

    BigDecimal cargo = 0.0

    String	sw2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String tipoDeCalculo


    static constraints = {
        serie maxSize: 20
        tipoDeCambio(scale:6)
        comentario nullable:true
        updateUser nullable: true
        createUser nullable: true
        usoDeCfdi maxSize:3
        sw2 nullable:true
        cfdi nullable: true
        tipoDeCalculo inList: ['PORCENTAJE','PRORRATEO']
    }

    static hasMany =[partidas:NotaDeCargoDet]

    static mapping = {
        id generator:'uuid'
        cliente index: 'NCARGO_IDX3'
        fecha type: 'date'
    }
}
