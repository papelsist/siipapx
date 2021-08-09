package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.utils.MonedaUtils

@ToString(includes = 'clave, descripcion', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'clave, moneda')
class PreciosPorClienteDet {

    String id

    PreciosPorCliente preciosPorCliente

    Producto producto

    String clave

    String descripcion

    Currency moneda = MonedaUtils.PESOS

    String tipoDeCambio = 1.0

    BigDecimal precioDeLista = 0.0

    BigDecimal descuento = 0.0

    BigDecimal precio = 0.0

    BigDecimal precioPorKilo = 0.0

    BigDecimal costo = 0.0

    BigDecimal costop = 0.0

    BigDecimal costou = 0.0

    String sw2

    static belongsTo = [preciosPorCliente:PreciosPorCliente]

    static constraints = {
        sw2 nullable: true
    }

    static mapping={
        id generator:'uuid'
    }

    def updateData(){
        this.clave = this.producto.clave
        this.descripcion = this.producto.descripcion
    }

    def beforeInsert(){
        updateData();
    }

    def beforeUpdate() {
        updateData();
    }

}
