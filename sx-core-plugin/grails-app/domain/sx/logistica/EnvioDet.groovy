package sx.logistica

import sx.core.Producto
import sx.core.VentaDet

class EnvioDet {

    String	id

    Envio	envio

    VentaDet ventaDet

    VentaParcialDet	parcialDet

    Producto producto

    BigDecimal cantidad	 = 0.0

    BigDecimal valor = 0.0

    BigDecimal kilos = 0.0

    String instruccionEntregaParcial

    Date dateCreated

    Date lastUpdated

    static  mapping ={
        id generator:'uuid'
    }

    static constraints = {
        instruccionEntregaParcial nullable: true
        parcialDet nullable: true
    }
}
