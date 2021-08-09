package sx.tesoreria

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Sucursal
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuenta

@ToString(includes = "folio, sucursal, origen, cuentaDeBanco,total",includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = 'id,folio')
class Ficha {

    String id

    Long folio = 0

    Sucursal sucursal

    String origen

    Date fecha

    // BigDecimal cheque = 0.0

    // BigDecimal efectivo = 0.0

    BigDecimal total = 0.0

    CuentaDeBanco cuentaDeBanco

    String tipoDeFicha

    // boolean envioForaneo = true

    Date fechaCorte

    Date cancelada

    MovimientoDeCuenta ingreso

    String comentario

    // String	tipo

    Boolean	envioValores	 = true

    String sw2

    // List partidas = []

    Date dateCreated

    Date lastUpdated


    // static hasMany =[partidas: FichaDet]

    static constraints = {
        sw2 nullable: true
        origen inList: ['CON','COD','CRE','CAM','CHE','JUR']
        comentario nullable: true
        tipoDeFicha inList:['EFECTIVO', 'OTROS_BANCOS', 'MISMO_BANCO']
        ingreso nullable:true
        cancelada nullable: true
        fechaCorte nullable: true
        // tipo nullable: true
    }

    static mapping ={
        id generator: 'uuid'
        fecha type:'date' , index: 'FICHA_IDX1'
        sucursal index: 'FICHA_IDX1'
        // partidas cascade: "all-delete-orphan"
        // cheque formula:"(select sum(X.cheque) FROM ficha_det X where X.ficha_id=id )"
        // efectivo formula:"(select sum(X.cheque) FROM ficha_det X where X.ficha_id=id )"
    }
}
