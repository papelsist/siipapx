package sx.logistica

class ModuloTipo {

    String id

    String modulo

    String tipo


    static constraints = {
        
    }

    static mapping = {
        id generator: 'uuid'
    }
}
