package sx.core

import grails.compiler.GrailsCompileStatic
import groovy.transform.ToString

@GrailsCompileStatic
@ToString( includeNames=true,includePackage=false)
class AuditLog {

    String name

    String tableName

    String source

    String target

    String persistedObjectId

    String eventName

    String message

    Date dateReplicated

    Date dateCreated
    Date lastUpdated


    static constraints = {
        name nullable: true
        tableName nullable: true
        persistedObjectId nullable: true
        eventName nullable: true
        dateReplicated nullable: true
        source nullable: true
        target nullable: true
        message nullable: true
        lastUpdated nullable: true
        dateCreated nullable: true
    }
}
