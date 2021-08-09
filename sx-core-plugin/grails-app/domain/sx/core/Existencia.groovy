package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includes = 'producto,cantidad,disponible', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'sucursal, producto, anio, mes')
class Existencia {

    String  id

    Sucursal sucursal

    Producto producto

    Long anio = 0

    Long mes = 0

    Boolean nacional = true

    BigDecimal kilos = 0

    /**
     * Acumuala en: CompraDet.cantidad, EntradaPorCompraDet.cantidad, y CompraDet.depuracion
     *
     */
    BigDecimal pedidosPendiente = 0

    /**
     * Acumula en EntradaPorCompraDet al persistir
     *
     */
    BigDecimal  entradaCompra    = 0

    /**
     *
     */
    BigDecimal  devolucionCompra     = 0

    /**
     *
     */
    BigDecimal  venta = 0

    /**
     *
     */
    BigDecimal  devolucionVenta  = 0

    /**
     * Acumula al persistir MovimientoDet.cantidad y al
     * Al inventariar MovimientoDet con Inventario.cantidad * -1
     */
    BigDecimal movimientoAlmacen = 0

    /**
     * Acumula TrasladoDet.cantidad
     *
     */
    BigDecimal  traslado     = 0

    /**
     *
     */
    BigDecimal  transformacion   = 0

    /**
     *
     */
    BigDecimal existenciaInicial = 0

    /**
     *
     */
    BigDecimal cantidad = 0


    /**
     *
     */
    BigDecimal disponible = 0

    String sw2

    String clave

    String sucursalNombre

    Date dateCreated

    Date lastUpdated

    Date fecha

    BigDecimal recorte = 0
    String recorteComentario
    Date recorteFecha

    static constraints = {
        pedidosPendiente nullable: true
        entradaCompra nullable: true
        devolucionCompra nullable: true
        venta nullable: true
        devolucionVenta nullable: true
        movimientoAlmacen nullable: true
        traslado nullable: true
        transformacion nullable: true
        sw2 nullable: true
        clave nullable: true
        sucursalNombre nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        recorte nullable: true
        recorteComentario nullable: true
        recorteFecha nullable: true
        fecha nullable: true
    }

    static mapping={
        id generator:'uuid'
        sucursal index: 'SUCURSAL_IDX'
        anio index: 'YEAR_IDX'
        mes index: 'MES_IDX'
        disponible formula:'cantidad - recorte + entrada_compra + devolucion_compra + venta + devolucion_venta + transformacion + traslado + movimiento_almacen'
        recorteFecha type: 'date'
        fecha type: 'date'
    }

    def beforeInsert() {
        updateDatos()
    }

    def beforeUpdate() {
        if(this.clave == null) {
            updateDatos()
        }
    }

    def updateDatos() {
        this.clave = this.producto.clave
        this.sucursalNombre = this.sucursal.nombre
    }
}
