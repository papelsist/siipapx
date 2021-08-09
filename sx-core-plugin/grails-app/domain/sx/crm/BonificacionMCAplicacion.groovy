package sx.crm

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, notaSerie, notaFolio')
class BonificacionMCAplicacion {

    String id

    BonificacionMC bonificacion

    Date fecha

    BigDecimal importe

    String sucursal

    String notaFolio

    String notaSerie

    String cobro

    Date notaFecha

    String uuid

    Date dateCreated
    Date lastUpdated

    String createUser
    String updateUser

    static constraints = {
        uuid nullable: true
        notaFolio nullable: true
        notaSerie nullable: true
        notaFecha nullable: true
        createUser nullable: true
        updateUser nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date'
        notaFecha type: 'date'

    }
}
