package sx.inventario

class RecorteDet {

    String	id

    Date	fecha

    BigDecimal	cantidad = 0

    String	comentario

    String	clasificacion

    String	responsable1

    String	responsable2

    Date dateCreated

    String createUser

    static constraints = {
        clasificacion inList:['CLIENTE','MOSTRADOR','CORTADOR','MATERIAL','GUILLOTINA','SURTIDOR','OTROS','SALIDA']
        responsable2 nullable: true
        comentario nullable: true
    }

    static mapping = {
        id generator:'uuid'
    }
}
