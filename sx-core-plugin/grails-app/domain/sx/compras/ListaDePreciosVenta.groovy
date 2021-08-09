package sx.compras

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false, excludes = ['lastUpdated', 'dateCreated','id','version'])
@EqualsAndHashCode(includeFields = true,includes = ['id','linea','descripcion'])
class ListaDePreciosVenta {

    String id

    String descripcion

    BigDecimal tipoDeCambioDolar = 1.0

    String linea = 'TODAS'

    Long folio = 0;

    Date inicio

    Date aplicada

    Long sw2

    List partidas = []

    String  autorizacion

    Date dateCreated

    Date lastUpdated

    static constraints = {
        aplicada nullable:true
        sw2 nullable:true
        tipoDeCambioDolar(scale:6)
        autorizacion nullable:true
    }

    static hasMany =[partidas:ListaDePreciosVentaDet]

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        inicio  index: 'LPV_DET_IDX1'
        aplicada  index: 'LPV_DET_IDX1'
        folio unique:true

    }


}
