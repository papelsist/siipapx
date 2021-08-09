package sx.tesoreria


import sx.sat.BancoSat


class Banco {

	String id

	String nombre

	BancoSat bancoSat

    Boolean nacional = true

    Long sw2

    static constraints = {
    	nombre unique: true
    	bancoSat nullable: true
        sw2 nullable: true
        nacional nullable:true
    }
    

    String toString(){
        return nombre
    }

    static mapping={
        id generator:'uuid'
    }
}
