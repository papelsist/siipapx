package sx.inventario

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Inventario
import sx.core.Sucursal
import sx.logistica.Chofer

@GrailsCompileStatic
@ToString( includes = "tipo, documento,fecha,comentario",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id')
class Traslado {

    String	id

    SolicitudDeTraslado	solicitudDeTraslado

    Inventario inventario

    Sucursal sucursal

    String	tipo

    Long	documento = 0

    Date	fecha

    Boolean	porInventario = false

    String	clasificacionVale

    BigDecimal  kilos     = 0

    String	comentario

    // String cfdiId

    List partidas =[]

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    Date asignado

    Chofer chofer

    Cfdi cfdi

    String uuid

    Date cancelado

    static hasMany = [partidas:TrasladoDet]

    static constraints = {
        sw2 nullable:true
        chofer nullable:true
        tipo nullable: true
        clasificacionVale nullable: true
        comentario nullable: true
        inventario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        porInventario nullable: true
        fechaInventario nullable: true
        // cfdiId nullable: true
        asignado nullable: true
        cfdi nullable: true
        uuid nullable: true
        cancelado nullable: true
    }

    static mapping = {
        id generator:'uuid'
        sucursal index:'SUCURSAL_IDX'
        fecha index: 'FECHA_IDX'
        partidas cascade: "all-delete-orphan"

    }
}
