package sx.logistica


import sx.core.Sucursal

class Embarque {


    String	id

    Date	cerrado

    Chofer	chofer

    String	comentario

    Integer	documento	 = 0

    Date	fecha

    BigDecimal	kilos	 = 0

    Date	regreso

    Date	salida

    Sucursal	sucursal

    BigDecimal	valor	 = 0

    List partidas = []

    String empleado

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser


    static hasMany = [partidas : Envio ]

    static constraints = {
        cerrado  nullable: true
        chofer nullable: true
        comentario nullable: true
        documento nullable: true
        regreso nullable: true
        salida nullable: true
        createUser nullable: true
        updateUser nullable: true
        empleado nullable:true
    }

    static mapping= {
        id generator: 'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type: 'date'
    }
}
