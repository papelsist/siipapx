package sx.logistica

import sx.security.User

class EntregaParcial {

    
    String id

    String venta

    String tipo

    String cliente

    String nombre

    String comentario

    Boolean	entregaLocal = true

    Long documento = 0

    String tipoDeVenta

    Date fecha

    Long folioFac = 0

    User facturo

    Date inicio

    String estado

    User autorizo

    Date dateCreated
    Date lastUpdated


   List<EntregaParcialDet> partidas =[]


    static hasMany = [partidas: EntregaParcialDet]


    static constraints = {

        comentario nullable: true
        folioFac nullable:true
        facturo nullable:true
        autorizo nullable:true
    }

    static mapping = {
        id generator: 'uuid'
        partidas cascade: "all-delete-orphan"
    }
}
