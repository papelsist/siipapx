package sx.contabilidad

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes='codigo')
class SatCuenta {

    String codigo

    String nombre

    String tipo

    Integer nivel



    static constraints = {
        codigo nullable:false,unique:true,maxSize:20
        tipo maxSize:100,nullable:true
    }


    static  mapping={
        id generator:'uuid'
    }

    String toString(){
        return "$codigo $nombre"
    }
}

