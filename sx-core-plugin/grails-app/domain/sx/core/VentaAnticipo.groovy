package sx.core

import sx.cxc.CuentaPorCobrar

class VentaAnticipo {


    String	id

    Venta venta

    BigDecimal importe	 = 0

    static constraints = {
        cuenta nullable: true
    }

    static  mapping = {
        id generator: 'uuid'
    }


}
