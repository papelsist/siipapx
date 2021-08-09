package sx.cxc

import grails.rest.Resource
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(excludes ='id,version,dateCreated,lastUpdated',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, nombre')
class DespachoDeCobranza {

    String id

    String nombre

    String rfc

    Boolean activo = true

    String telefono1

    String telefono2

    String telefono3

    Set<String> abogados

    Date dateCreated

    Date lastUpdated

    static hasMany = [abogados: String]

    static constraints = {
        rfc size:12..13
        nombre unique: true
        telefono1 nullable:true ,maxSize:30
        telefono2 nullable:true ,maxSize:30
        telefono3 nullable:true ,maxSize:30
    }

    static mapping={
        id generator:'uuid'
        abogados joinTable: [name: 'despacho_abogados',
                              key: 'despacho_id',
                              column: 'abogado',
                              type: "text"]
    }

}
