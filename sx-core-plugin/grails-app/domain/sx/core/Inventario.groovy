package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString( includes = "sucursal,documento,fecha,cantidad",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id')
class Inventario {

    String	id

    Sucursal sucursal

    Producto producto

    Date fecha

    Long documento = 0

    String tipo

    String tipoVenta

    BigDecimal cantidad = 0

    BigDecimal kilos = 0

    Boolean	nacional = true

    BigDecimal costo = 0

    BigDecimal costoPromedio = 0.0

    BigDecimal costoUltimaCompra = 0.0

    BigDecimal costoReposicion = 0.0

    String comentario

    String sw2

    Integer renglon

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String clave

    String descripcion

    String sucursalNombre

    static constraints = {
        tipo nullable: true
        comentario nullable: true
        sw2 nullable: true
        tipoVenta nullable: true
        renglon nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        clave nullable: true, maxSize:30
        descripcion nullable: true
        sucursalNombre nullable: true, maxSize:40
    }

    static mapping ={
        id generator:'uuid'
        producto index:'PRODUCTO_IDX'
        sucursal index:'SUCURSAL_IDX'
        fecha index: 'FECHA_IDX'
    }

    def afterInsert() {
        // def factor = this.producto.unidad == 'MIL' ? 1000 : 1;
        //  this.kilos = (this.cantidad / factor) * this.producto.kilos
    }

    def beforeInsert() {
        updateDatos()
        updateKilos()
    }

    def beforeUpdate() {
        if(this.clave == null) {
            updateDatos()
            updateKilos()
        }
    }

    def updateDatos() {
        this.clave = this.producto.clave
        this.descripcion = this.producto.descripcion
        this.sucursalNombre = this.sucursal.nombre
    }

    def updateKilos(){
        def factor = this.producto.unidad == 'MIL' ? 1000 : 1;
        this.kilos = (this.cantidad / factor) * this.producto.kilos
    }


}
