package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes ='id,tipo,solicito,autorizo,venta',includeNames=true,includePackage=false)
@EqualsAndHashCode
class AutorizacionDeVenta {

    String id

    String tipo;

    Venta venta;

    String solicito

    String autorizo

    String comentario

    Date fecha

    Date dateCreated
    Date lastUpdated

    static constraints = {
        comentario nullable:true
        tipo inList: ['SIN_EXISTENCIA', 'DESCUENTO_ESPECIAL']
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date', index: 'AUT_IDX1'
        solicito index: 'AUT_IDX2'
        autorizo index: 'AUT_IDX3'
    }
}
