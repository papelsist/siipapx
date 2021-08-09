package sx.core

import sx.core.Direccion

class DireccionEntrega {

    String id

    Cliente cliente

    Direccion direccion

    static embedded = ['direccion']

    static constraints = {
    }

    static mapping = {
        id generator: 'uuid'
    }
}
