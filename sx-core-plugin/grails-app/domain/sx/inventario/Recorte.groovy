package sx.inventario


import sx.core.Producto
import sx.core.Sucursal

class Recorte {

    String	id

    Producto producto

    Sucursal sucursal

    String	comentario

    List partidas = []

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    static hasMany =[partidas:RecorteDet]

    static constraints = {
        comentario nullable : true

    }
    static  mapping = {
        id generator: 'uuid'
        partidas cascade: "all-delete-orphan"
    }
}
