package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

/**
 * Created by Ruben Cancino 1/12/2020
 */
@ToString( includes = ['cxcTipo', 'cxcDocumento', 'importe'], includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id','cobro'])
class AnticipoSatDet {

    String id
    Date fecha
    
    String cxc 
    Long cxcDocumento
    
    String cxcTipo
    String uuid
    
    String moneda = 'MXN'
    BigDecimal tipoDeCambio = 1.0

    String egresoCfdi
    String egresoUrl
    String egresoUuid
    
    String cobro

    BigDecimal importe
    
    String comentario

    Date dateCreated
    Date lastUpdated

    String createUser
    String updateUser

    static constraints = {
        tipoDeCambio(scale:6)
        moneda maxSize: 5
        cobro unique: true
        uuid nullable: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true

        egresoCfdi nullable: true
        egresoUrl nullable: true
        egresoUuid nullable: true
    }

    static mapping = {
        id generator:'uuid'
        fecha type: 'date'
        sort 'dateCreated'
    }

    static belongsTo = [anticipo: AnticipoSat]

    Map toFirebase() {
        Map data = filter(this.properties)
        data.importe = this.importe.toDouble()
        data.tipoDeCambio = this.tipoDeCambio.toDouble()
        data.id = this.id
        return data
    }
    
    Map filter(Map data) {
        data = data.findAll{ k, v -> !['class','constraints', 'errors', 'anticipo', 'anticipoId'].contains(k) }
        return data
    }
    
}
