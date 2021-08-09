package sx.core

class Marca {

    String id

	String marca

    Boolean activa = true

	Date dateCreated

	Date lastUpdated

    static constraints = {
        marca minSize:3, maxSize:20, unique:true
    }

    String toString(){
    	return marca
    }

    static mapping={
        id generator:'uuid'
    }

    
}