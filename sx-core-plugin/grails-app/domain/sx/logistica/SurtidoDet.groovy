package sx.logistica

class SurtidoDet {

    String id

    String clave

    String descripcion

    Surtido surtido

    BigDecimal	cantidad	 = 0

    BigDecimal	valor	 = 0

    String	instruccionDeEntregaParcial

    String entregaParcialDet


    Date dateCreated
    Date lastUpdated

    static belongsTo = [surtido: Surtido]

    static constraints = {
        clave nullable:true
        descripcion nullable: true
        instruccionDeEntregaParcial nullable: true
    }

     static mapping = {
        id generator: 'uuid'
    }
}
