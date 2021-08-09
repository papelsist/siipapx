package sx.core


class Clase {

    String id

	String clase

    Boolean activa =  true

	Date dateCreated

	Date lastUpdated

    static constraints = {
        clase minSize:2, maxSize:50, unique:true
    }

    String toString(){ 
    	this.clase
    }

    static mapping={
        id generator:'uuid'
    }
    
    
}