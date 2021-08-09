package sx.core

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes='id',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id')
class ProveedorProducto {

	String id

	Proveedor proveedor

	Producto producto

	String claveProveedor

	String codigoProveedor

	String descripcionProveedor

	Integer paqueteTarima = 0

	Integer piezaPaquete = 0

	BigDecimal precio= 0

	Date fecha

	Date dateCreated

	Date lastUpdated




    static constraints = {
		claveProveedor nullable:true
        codigoProveedor nullable:true
        descripcionProveedor nullable:true
		fecha nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
    }

    static mapping = {
    	id generator:'uuid'
    }
}
