package sx.logistica


class FacturistaDeEmbarque {

    String id

    String nombre

    String rfc

    String telefono

    String email

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    static constraints = {
        createUser nullable: true
        updateUser nullable: true
        sw2 nullable:  true
    }

    static mapping= {
        id generator: 'uuid'
        rfc minSize: 12, maxSize: 13
        telefono nullbale: true
    }

    String toString() {
        return "$nombre"
    }
}
