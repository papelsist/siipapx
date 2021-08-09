package sx.logistica

class CodigosPostalesMx {


    String	id

    String codigo

    String colonia

    String asentamiento

    String municipio

    String estado

    String ciudad


    static constraints = {
        id nullable: true
        codigo nullable: true
        colonia nullable: true
        asentamiento nullable: true
        municipio nullable: true
        estado nullable: true
        ciudad nullable: true

    }

    static mapping = {
        id generator:'uuid'
    }

}
