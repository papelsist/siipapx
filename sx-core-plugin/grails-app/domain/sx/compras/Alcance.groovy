package sx.compras

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal,clave, fecha')
@GrailsCompileStatic
class Alcance {

    String	id

    String sucursal

    Date fecha = new Date()
    Date fechaInicial
    Date fechaFinal
    Integer meses

    String clave
    String descripcion
    String linea
    String marca
    String clase
    String unidad
    BigDecimal kilos


    String proveedor
    String nombre

    BigDecimal existencia = 0.0
    BigDecimal existenciaEnToneladas = 0.0
    BigDecimal venta = 0.0
    BigDecimal mesesPeriodo = 0.0
    BigDecimal promVta = 0.0
    BigDecimal promVtaEnTonelada = 0.0
    BigDecimal alcance = 0.0
    BigDecimal comprasPendientes = 0.0
    BigDecimal promVtaEnToneladas = 0.0

    String comentario

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    BigDecimal alcanceMasPedido = 0.0
    BigDecimal porPedir = 0.0

    static constraints = {
        existencia scale: 4
        existenciaEnToneladas scale: 4
        venta scale: 4
        mesesPeriodo scale: 4
        promVta scale: 4
        promVtaEnTonelada scale: 4
        clave unique: ['sucursal', 'fecha']
        alcance scale: 4
        comprasPendientes scale: 4
        promVtaEnToneladas scale: 4
        linea nullable: true
        marca nullable: true
        clase nullable: true
        proveedor nullable: true
        nombre  nullable: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type: 'date', index: 'ALCANCE_FECHA_IDX'
        fechaInicial type: 'date'
        fechaFinal type: 'date'
    }

    static transients = ['alcanceMasPedido', 'porPedir']


    BigDecimal getAlcanceMasPedido() {
        if(promVta){
            return (comprasPendientes + existencia) / promVta
        }
        return 0
    }

    BigDecimal getPorPedir() {
        // return getAlcanceMasPedido() * promVta
        def res = (promVta * meses) - comprasPendientes - existencia
        return res > 0 ? res: 0
    }

    BigDecimal getPorPedirKilos() {
        def porPedir = getPorPedir()
        return porPedir * kilos
    }
}
