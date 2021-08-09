package sx.inventario

import sx.core.Inventario
import sx.core.Producto

class TransformacionDet {

    String	id

    Inventario inventario

    Producto producto

    Transformacion transformacion

    TransformacionDet	destino

    BigDecimal	cantidad	 = 0

    Long	cortes	 = 0

    String	cortesInstruccion

    String	comentario

    String sw2

    Date dateCreated

    Date lastUpdated



    static belongsTo = [transformacion:Transformacion]

    static constraints = {
        destino nullable: true
        cortesInstruccion nullable: true
        comentario nullable: true
        sw2 nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        inventario nullable: true
    }


    static mapping ={
        id generator:'uuid'
        producto index:'PRODUCTO_IDX'

    }

}
