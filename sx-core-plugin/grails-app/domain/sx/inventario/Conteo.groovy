package sx.inventario

import sx.core.Sucursal

class Conteo {


    String	id

    Long	documento	 = 0

    Sucursal sucursal

    Date	fecha

    Sector	sector

    String	auditor1

    String	auditor2

    String	capturista

    String	comentario

    String	contador1

    String	contador2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    List partidas = []


    static  hasMany = [partidas:ConteoDet]

    static constraints = {
        auditor1 nullable: true
        auditor2 nullable: true
        comentario nullable: true
        contador1 nullable: true
        contador2 nullable: true
        capturista nullable: true
        updateUser nullable: true
        createUser nullable: true
    }

    static  mapping = {
        id generator: 'uuid'
        fecha type: 'date'
    }
}
