package sx.logistica

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.InstruccionCorte
import sx.core.Producto
import sx.security.User

@GrailsCompileStatic
@ToString( includes = "clave",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id')
class Corte {

    String	id

    String clave

    String descripcion

    String origen

    String instruccion

    User	cortador

    User	empacador

    User	cancelo

    Date	inicio

    Date	fin

    Date	empacadoInicio

    Date	empacadoFin

    Date	cancelado

    Date	asignado

    Surtido surtido

    String  estado

    Long factura

    Long venta

    BigDecimal cantidad

    Boolean parcial = false

    Boolean parcializado = false

    Date dateCreated
    Date lastUpdated

    List    auxiliares = []

    static belongsTo = [surtido: Surtido]

    static hasMany = [auxiliares:AuxiliarCorte]

    static constraints = {
        cortador nullable: true
        asignado nullable: true
        empacador nullable: true
        cancelo nullable: true
        inicio nullable: true
        fin nullable: true
        empacadoFin nullable: true
        empacadoInicio nullable: true
        cancelado nullable: true
        auxiliares nullable: true
        origen nullable: true
        instruccion nullable:true
        factura nullable:true
        venta nullable:true
        cantidad nullable:true
    }

    static mapping = {
        id generator:'uuid'
        auxiliares cascade: "all-delete-orphan"
        cancelado type:'date'
    }
}
