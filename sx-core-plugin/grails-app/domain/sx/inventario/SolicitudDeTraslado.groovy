package sx.inventario

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Sucursal
import sx.core.Venta

@GrailsCompileStatic
@ToString(includes = 'sucursalSolicita, sucursalAtiende, comentario', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id')
class SolicitudDeTraslado {

    String	id
    
    Sucursal sucursalSolicita

    Sucursal	sucursalAtiende

    Long	documento	 = 0

    Date	fecha

    String	referencia

    String venta

    String	clasificacionVale = 'EXISTENCIA'

    Boolean	noAtender	 = false

    String	comentario

    List partidas =[]

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    Date fechaInventario

    Date atender

    Boolean cancelado=false
    

    static hasMany = [partidas:SolicitudDeTrasladoDet]

    static constraints = {
        referencia nullable:true
        venta nullable: true
        clasificacionVale nullable: true
        comentario nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        sw2 nullable: true
        fechaInventario nullable: true
        atender nullable: true
        
    }

    static mapping = {
        id generator:'uuid'
        fecha index:'FECHA_IDX'
    }

}
