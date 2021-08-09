package sx.core

class ProveedorContactos {

    String	id

    Boolean	activo	 = true

    Proveedor	proveedor

    String	nombre

    String	puesto

    String	email

    String	telefono

    Long	sw2	 = 0


    static constraints = {
        nombre nullable:true
        puesto nullable: true
        email nullable:  true
        telefono nullable: true

    }

    static  mapping={
        id generator:'uuid'
    }
}
