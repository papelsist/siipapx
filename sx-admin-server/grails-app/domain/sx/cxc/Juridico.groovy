package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString( excludes = "id, version", includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true, includes = ['id','cxc'])
class Juridico {

    String id

    String nombre

    CuentaPorCobrar cxc

    BigDecimal importe

    BigDecimal saldo

    String comentario

    DespachoDeCobranza despacho

    String abogado

    Date traspaso

    Date asignacion

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    static constraints = {
        comentario nullable: true
        asignacion nullable: true
        createUser nullable: true
        updateUser nullable: true
    }

    static mapping= {
        id generator: 'uuid'
        traspaso type: 'date'
        asignacion type: 'date'
    }

}
