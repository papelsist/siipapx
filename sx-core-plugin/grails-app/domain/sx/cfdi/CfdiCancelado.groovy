package sx.cfdi

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false, excludes = ['dateCreated', 'lastUpdated'])
@EqualsAndHashCode(includeFields = true, includes = ['uuid', 'id'])
class CfdiCancelado {

    String id

    String uuid

    String serie

    String folio

    String comentario

    byte[] aka

    String statusSat

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    Cfdi cfdi

    static constraints = {
        serie maxSize:30
        folio maxSize:30
        uuid unique:true
        comentario nullable:true
        aka maxSize:(1024 * 512)  // 50kb para almacenar el xml
        createUser nullable: true
        updateUser nullable: true
        statusSat maxSize: 10
    }

    static  mapping={
        id generator:'uuid'
    }

    static transients  = ['cfdi']

    Cfdi getCfdi() {
        return Cfdi.where {uuid == this.uuid}.find()
    }

    def afterInsert() {

    }

}
