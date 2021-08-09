package sx.logistica


class Transporte {

    String id

    String numero

    String descripcion

    String placa

    String marca

    String modelo

    String anio

    BigDecimal capacidad = 0.0

    Chofer chofer

    FacturistaDeEmbarque facturista

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    static constraints = {
        createUser nullable: true
        updateUser nullable: true
        numero maxSize:30
        placa nullable:true
        marca nullable:true
        modelo nullable:true
        anio nullable: true
        sw2 nullable: true
        chofer nullable: true
        facturista nullable: true
    }

    static mapping= {
        id generator: 'uuid'
    }

    String toString() {
        return "$numero"
    }
}
