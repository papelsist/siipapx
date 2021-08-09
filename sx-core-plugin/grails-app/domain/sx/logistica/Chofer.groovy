package sx.logistica

class Chofer {

    String id

    String nombre

    String rfc

    String celular

    String mail

    String sw2

    Date dateCreated

    Date lastUpdated

    String createdBy

    String lastUpdatedBy



    static constraints = {
        mail nullable: true
        celular nullable: true
        rfc nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createdBy nullable: true
        lastUpdatedBy nullable: true
        sw2 nullable: true
    }

    static  mapping={
        id generator:'uuid'
    }
}
