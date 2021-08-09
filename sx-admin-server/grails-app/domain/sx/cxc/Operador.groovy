package sx.cxc

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes='id')
class Operador {

    String clave
    String nombre

    Date dateCreated
    Date lastUpdated


    static constraints = {
        nombre unique: true
        clave unique: true
    }

    String toString() {
        this.nombre
    }
}
