package sx.sat


import grails.rest.*

//@Resource(uri='/api/sat/cuentas', formats=['json'])
class CuentaSat {

    String id
	
	String codigo

    String nombre

    String tipo

    Integer nivel

    static constraints = {
        codigo nullable:false,unique:true,maxSize:20
        tipo maxSize:100,nullable:true
    }

    String toString(){
        return "$codigo $nombre"
    }

    static  mapping={
        id generator:'uuid'
    }
}