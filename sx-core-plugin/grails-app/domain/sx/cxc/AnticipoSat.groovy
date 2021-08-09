package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

/**
 * Created by Ruben Cancino 1/12/2020
 */
@ToString( includes = ['cliente','fecha', 'uuid', 'total'], includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','uuid'])
class AnticipoSat {

    String	id
    String sucursal
    String cliente
    String nombre
    String rfc
    Date fecha
    
    String cxc
    Long cxcDocumento
    
    String moneda = 'MXN'
    BigDecimal tipoDeCambio = 1.0
    
    String cfdi
    String cfdiSerie
    String cfdiFolio
    String uuid

    BigDecimal importe
    BigDecimal impuesto
    BigDecimal total
    BigDecimal disponible
    
    String comentario

    Set<AnticipoSatDet> aplicaciones = []
    

    Date dateCreated
    Date lastUpdated

    String createUser
    String updateUser


    static constraints = {
        tipoDeCambio(scale:6)
        moneda maxSize: 5
        uuid unique: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true
    }


    static mapping = {
        id generator:'uuid'
        fecha type: 'date'
        disponible formula:'total - (select COALESCE(sum(x.importe),0) from cobro x where x.anticipo_sat=id)'
        aplicaciones cascade: "all-delete-orphan"
    }

    static hasMany =[aplicaciones: AnticipoSatDet]

    static transients = ['folio']

    String getFolio() {
        return "${cfdiSerie}-${cfdiFolio}" as String
    }

    Map toFirebase() {
        Map data = filter(this.properties)
        data.id = this.id
        data.aplicaciones = this.aplicaciones.collect{ item -> item.toFirebase()}
        data.folio = this.getFolio()
        data.importe = this.importe.toDouble()
        data.impuesto = this.impuesto.toDouble()
        data.total = this.total.toDouble()
        data.disponible = this.disponible.toDouble()
        data.tipoDeCambio = this.tipoDeCambio.toDouble()
        return data
    }

    Map filter(Map data) {
        data = data.findAll{ k, v -> !['class','constraints', 'errors', 'aplicaciones'].contains(k) }
        return data
    }
}
