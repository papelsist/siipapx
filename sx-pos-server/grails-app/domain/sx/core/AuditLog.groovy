package sx.core


class AuditLog {

    Date dateCreated

    Date lastUpdated

    String name

    String tableName

    String source

    String target

    String persistedObjectId

    String eventName

    String message

    Date dateReplicated


    static constraints = {

        name nullable: true
        tableName nullable: true
        persistedObjectId nullable: true
        eventName nullable: true
        dateReplicated nullable: true
        source nullable: true
        target nullable: true
        lastUpdated nullable: true
        dateCreated nullable: true
        message nullable: true
    }
}
