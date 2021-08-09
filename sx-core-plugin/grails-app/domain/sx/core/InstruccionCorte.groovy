package sx.core

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@GrailsCompileStatic
@ToString(includes = 'cantidad,precio,instruccion', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id,ventaDet')
class InstruccionCorte {

    String id

    Long cantidad = 0

    BigDecimal	precio = 0

    BigDecimal ancho = 0

    BigDecimal largo = 0

    Boolean refinado = false

    String seleccionCalculo

    String tipo

    String instruccion

    String instruccionEmpacado

    VentaDet ventaDet

    static constraints = {
        seleccionCalculo nullable: true
        tipo nullable: true, maxSize: 50//inList:['CALCULADO','CRUZ','CARTA','MITAD','1/8','CROQUIS','DOBLE_CARTA','MEDIA_CARTA','OFICIO']
        instruccion nullable: true
        instruccionEmpacado nullable: true
    }

    static  mapping ={
        id generator:'uuid'
    }

    // static belongsTo = [ventaDet: VentaDet]
    // static belongsTo = [ventaDet:Venta]

}
