package sx.logistica

import sx.core.Producto
import sx.core.VentaDet

class VentaParcialDet {



    String	id

    VentaDet ventaDet

    Producto producto

    BigDecimal	cantidad	 = 0

    BigDecimal	valor	 = 0

    String	instruccionDeEntregaParcial

    //List partidas =  []

   // static hasMany =[partidas:VentaDet]

    static constraints = {
        ventaDet nullable: true
        producto nullable: true
        instruccionDeEntregaParcial nullable: true
    }


    static mapping = {
        id generator:'uuid'
    }
}
