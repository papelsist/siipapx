package sx.logistica

import sx.security.User

class SurtidoAuxiliar {

    String	id

    Surtido	surtido

    User auxiliarSurtido

    Date	dateCreated


    static constraints = {
    }

    static mapping = {
        id generator: 'uuid'
    }
}
