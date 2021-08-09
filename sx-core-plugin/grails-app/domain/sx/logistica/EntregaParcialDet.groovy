package sx.logistica

class EntregaParcialDet {


    String	id

    String ventaDet

    String producto

    String clave

    String descripcion

    BigDecimal	cantidad	 = 0

    BigDecimal	valor	 = 0

    BigDecimal entregado = 0

    BigDecimal porEntregar 

    String	instruccionDeEntregaParcial

    EntregaParcial entregaParcial


    Date dateCreated
    Date lastUpdated

    static belongsTo = [entregaParcial: EntregaParcial]

    static transients = ['porEntregar']
  
    static constraints = {
        ventaDet nullable: true
        producto nullable: true
        clave nullable:true
        descripcion nullable: true
        instruccionDeEntregaParcial nullable: true
    }

    static mapping = {
        id generator:'uuid'
        entregado formula:'(select COALESCE(sum(x.cantidad),0) from surtido_det x where x.entrega_parcial_det=id)'
    }

}
