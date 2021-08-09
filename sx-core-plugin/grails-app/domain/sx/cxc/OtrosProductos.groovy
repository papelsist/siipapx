package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(excludes = ["id,lastUpdated,dateCreated"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class OtrosProductos {


    String	id

    Cobro	cobro

    BigDecimal	diferencia	 = 0

    Date	diferencia_Fecha

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    static mapping={
        id generator: 'uuid'

    }

    static constraints = {
        diferencia_Fecha nullable: true
    }
}
