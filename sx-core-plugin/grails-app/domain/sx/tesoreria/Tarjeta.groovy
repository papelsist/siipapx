package sx.tesoreria

class Tarjeta {

    String id

    static constraints = {
    }

    static mapping = {
        id generator: 'uuid'
    }
}
