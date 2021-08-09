package sx.crm

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente

@ToString(excludes = 'dateCreated,lastUpdated,version',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, nombre')
class BonificacionMC {

    String id

    Integer ejercicio

    Integer mes

    Cliente cliente

    String nombre

    Date fecha

    BigDecimal ventas

    BigDecimal ventasKilos

    BigDecimal facturas

    BigDecimal bono = 0.0

    BigDecimal importe = 0.0

    BigDecimal aplicado = 0.0

    BigDecimal disponible

    BigDecimal ajuste = 0.0

    Integer vigenciaDias = 90

    Date vencimiento

    Date suspendido

    String suspendidoComentario

    Date ultimaVenta

    Date ultimaAplicacion

    Date autorizado

    int posicion

    Date dateCreated
    Date lastUpdated

    static constraints = {
        vencimiento nullable: true
        suspendido nullable: true
        suspendidoComentario nullable: true
        ultimaAplicacion nullable: true
        vencimiento nullable: true
        autorizado nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type:'date'
        vencimiento type:'date'
        suspendido type:'date'
        ultimaAplicacion type:'date'
        ultimaVenta type: 'date'
        autorizado type: 'date'
    }

    static transients = ['disponible']

    def getDisponible() {
        return this.importe - this.aplicado - this.ajuste
    }
}
