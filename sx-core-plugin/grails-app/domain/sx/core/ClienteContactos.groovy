package sx.core

class ClienteContactos {


    String	id

    Boolean	activo	 = true

    String	nombre

    String	puesto

    String	email

    String	telefono

    Long	sw2	 = 0

    Cliente cliente




    static constraints = {
        nombre nullable:true
        puesto nullable: true
        email nullable: true
        telefono nullable: true
        sw2 nullable: true
    }

    static mapping={
        id generator:'uuid'
    }



}
