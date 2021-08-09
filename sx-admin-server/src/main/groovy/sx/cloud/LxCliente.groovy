package sx.cloud

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

import sx.core.Direccion
import sx.core.Cliente

@ToString(includeNames=true, includePackage=false)
@EqualsAndHashCode(includes='nombre')
class LxCliente {

    String id
    String clave
    String nombre
    String rfc
    String email

    Long folioRFC = 1
    
    Boolean activo = true
    Boolean permiteCheque = false
    Double chequeDevuelto = 0
    Boolean juridico = false
    
    
    Map direccion

    List medios = []

    Map credito

    Date dateCreated
    Date lastUpdated
    String createUser
    String updateUser
    

    public LxCliente() {}

    public LxCliente(Cliente cliente) {
        this.id = cliente.id
        this.clave = cliente.clave
        this.nombre = cliente.nombre
        this.rfc = cliente.rfc
        this.email = cliente.getCfdiMail() 
        this.activo = cliente.activo
        this.medios = parseMedios(cliente)
        this.folioRFC = cliente.folioRFC
        this.activo = cliente.activo
        this.permiteCheque = cliente.permiteCheque
        this.chequeDevuelto = cliente.chequeDevuelto
        this.juridico = cliente.juridico
        this.direccion = cliente.direccion.toFirebaseMap()
        this.credito = cliente.credito ? new LxClienteCredito(cliente.credito).toMap() : null
        lastUpdated = cliente.lastUpdated
        dateCreated = cliente.dateCreated
        createUser = cliente.createUser
        updateUser = cliente.updateUser
    }

    def copyProperties(source, target) {
        def (sProps, tProps) = [source, target]*.properties*.keySet()
        def commonProps = sProps.intersect(tProps) - ['class', 'metaClass', 'additionalMetaMethods']
        commonProps.each { target[it] = source[it] }
    }

    List parseMedios(Cliente c) {
        return c.medios.collect{ item -> [
            id: item.id,
            activo: item.activo,
            tipo: item.tipo,
            descripcion: item.descripcion,
            comentario: item.comentario,
            cfdi: item.cfdi,
            validado: item.validado
        ]}
    }

    Map toMap() {
        Map data = this.properties
        return filter(data)
    }
    Map filter(Map data) {
        data = data.findAll{ k, v -> !['class','constraints', 'errors'].contains(k) }
        return data
    }

    

}
