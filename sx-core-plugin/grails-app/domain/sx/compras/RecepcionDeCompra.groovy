package sx.compras

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Proveedor
import sx.compras.Compra
import sx.core.Sucursal
import sx.compras.RecepcionDeCompraDet

@ToString(includes = 'remision,sucursal,fecha,comentario',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal,documento')
class RecepcionDeCompra {

    String id

    Long documento = 0

    String remision

    Date fechaRemision

    Compra compra

    Proveedor proveedor

    Sucursal sucursal

    Date fecha

    String comentario

    List partidas = []

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    Date cancelado

    String sw2

    Date fechaInventario


    static hasMany =[partidas:RecepcionDeCompraDet]

    static constraints = {
        comentario nullable:true
        sw2 nullable:true
        remision nullable: true
        fechaRemision nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        fechaInventario nullable: true
        cancelado nullable:true
    }



    static mapping = {
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type:'date', index: 'RECOMPRA_IDX1'
    }



}
