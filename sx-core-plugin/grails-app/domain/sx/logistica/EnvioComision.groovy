package sx.logistica

import sx.core.Venta
import sx.logistica.Envio

class EnvioComision {

    String	id

    Envio envio

    Venta venta

    BigDecimal	valor	 = 0

    Date	fechaComision

    String	comentarioDeComision

    BigDecimal	comisionPorTonelada	 = 0

    BigDecimal	importeComision	 = 0

    BigDecimal	comision	 = 0

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser




    static constraints = {
        fechaComision nullable:true
        comentarioDeComision nullable: true
        venta nullable: true
    }

    static  mapping = {
        id generator:'uuid'
    }
}
