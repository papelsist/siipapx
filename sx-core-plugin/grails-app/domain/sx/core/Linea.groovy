package sx.core


class Linea {

    String id

	String linea

    Boolean activa = true

	Date dateCreated

	Date lastUpdated

    static constraints = {
        linea minSize:2, maxSize:50, unique:true
    }

    String toString(){
    	return linea
    }

    static mapping={
        id generator:'uuid'
    }
}
