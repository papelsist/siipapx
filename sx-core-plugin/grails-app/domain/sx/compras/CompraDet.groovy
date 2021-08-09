package sx.compras

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import sx.core.Producto
import sx.core.Sucursal


@ToString(includes = 'producto,solicitado,importeNeto', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id')
class CompraDet {

    String id

    Compra compra

    Producto producto
    String clave
    String descripcion
    String unidad

    Sucursal sucursal

    BigDecimal solicitado = 0.0

    BigDecimal precio = 0.0

    BigDecimal importeBruto = 0.0

    BigDecimal descuento1 = 0.0

    BigDecimal descuento2 = 0.0

    BigDecimal descuento3 = 0.0

    BigDecimal descuento4 = 0.0

    BigDecimal importeDescuento = 0.0

    BigDecimal descuentoFinanciero = 0.0

    BigDecimal costo = 0.0

    BigDecimal importeNeto = 0.0

    Date depuracion

    BigDecimal depurado = 0.0

    BigDecimal recibido = 0.0

    BigDecimal porRecibir

    String comentario

    String sw2

    Date dateCreated

    Date lastUpdated

    static constraints = {
        comentario nullable: true
        clave nullable: true, maxSize: 20
        descripcion nullable: true
        unidad nullable: true, maxSize: 10
        depuracion nullable: true
        sw2 nullable:true
    }

    static mapping = {
        id generator: 'uuid'
        recibido formula:'(select COALESCE(sum(x.cantidad),0) from recepcion_de_compra_det x where x.compra_det_id=id)'
    }

    static belongsTo = [compra:Compra]

    static transients = ['porRecibir']

    public BigDecimal getPorRecibir() {
        return this.solicitado - this.depurado - this.recibido
    }



}
