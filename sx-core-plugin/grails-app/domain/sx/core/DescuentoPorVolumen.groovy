package sx.core

class DescuentoPorVolumen {


    String	id

    Boolean	activo	 = true

    BigDecimal	descuento	 = 0

    BigDecimal	importe	 = 0

    Date	dateCreated

    static constraints = {
        id generator:'uuid'
    }
}
