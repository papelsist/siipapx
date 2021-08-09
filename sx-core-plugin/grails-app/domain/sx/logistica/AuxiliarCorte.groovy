package sx.logistica

import sx.security.User

class AuxiliarCorte {
    
    String id

    Corte corte

    User auxiliarCorte

    String tipo

    Date dateCreated



    static constraints = {
        tipo inList:['EMPACADOR','CORTADOR']
    }

    static  mapping = {
        id generator: 'uuid'
    }
}
