package sx.inventario

import sx.core.Sucursal

class Sector {

    String id

    Sucursal sucursal

    Long	sectorFolio	 = 0

    String	comentario

    String	responsable1

    String	responsable2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    List partidas = [];

    static  hasMany = [partidas:SectorDet]

    static constraints = {
        responsable1 nullable: true
        responsable2 nullable: true
        comentario nullable: true
        updateUser nullable: true
        createUser nullable: true
    }

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
    }
}
