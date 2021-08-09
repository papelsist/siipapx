package sx.core

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes = 'tipo,descripcion, comentario, activo, cfdi',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, tipo, descripcion,comentario')
class ComunicacionEmpresa {

    String	id

    Boolean	activo	 = true

    String	tipo

    String	descripcion

    String	comentario

    Boolean cfdi = false

    Long	sw2	 = 0

    Cliente cliente

    Boolean validado = false;

    String createUser

    String updateUser

    String sucursalCreated
    String sucursalUpdated


    static constraints = {
        tipo inList:['TEL','CEL','FAX','MAIL','WEB']
        descripcion nullable:true
        comentario  nullable:true
        sw2 nullable: true
        validado nullable: true
        createUser nullable: true
        updateUser nullable: true
        sucursalCreated nullable: true
        sucursalUpdated nullable: true

    }
    static  mapping={
        id generator: 'assigned'
    }

    static belongsTo = [cliente: Cliente]

}
