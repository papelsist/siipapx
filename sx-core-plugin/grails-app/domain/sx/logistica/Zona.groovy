package sx.logistica

class Zona {


    String	id

    String	divisionZona

    Long	entidad_id	 = 0

    String	entidad

    String	cp_ini

    String	cp_fin

    Long	sector

    String	asignacion

    String	area



    static constraints = {
        divisionZona nullable:true

    }

    static mapping = {
        id generator:'uuid'
    }

}
