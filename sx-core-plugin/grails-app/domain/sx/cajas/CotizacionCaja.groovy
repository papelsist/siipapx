package sx.cajas

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.core.Producto

import sx.core.Sucursal

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal')

class CotizacionCaja {

    String	id

    Producto producto

    Sucursal sucursal

    Date fecha = new Date()

    Boolean cotizacion = true 

    Long folio

    String tipo

    String resistenciaECT

    String flauta

    BigDecimal largo = 0.00

    BigDecimal ancho = 0.00

    BigDecimal altura = 0.00

    BigDecimal metrosLineales = 0.00

    Long piezas = 0

    BigDecimal costo = 0.00

    BigDecimal precioPiezaContado = 0.00

    BigDecimal precioPiezaCredito = 0.00

    BigDecimal precioEspecialCredito = 0.00

    BigDecimal precioEspecialContado = 0.00

    BigDecimal kilos = 0.00

    BigDecimal gramos = 0.00

    Boolean cerrada = false

    String comentario

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String claveCaja

    String descripcionCaja

    String productoClave

    String productoDescripcion

    BigDecimal productoPrecioContado

    BigDecimal productoPrecioCredito

    static constraints = {
        folio nullable: true
        tipo nullable: true
        resistenciaECT nullable: true
        flauta nullable: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true
        productoClave nullable:true
        productoDescripcion nullable:true
        productoPrecioContado nullable:true
        productoPrecioCredito nullable:true
        kilos nullable:true
        gramos nullable:true
    }

    static mapping = {
        id generator:'uuid'
        fecha type: 'date'
    }

}
