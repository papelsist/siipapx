package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes='nombre')
@ToString(includeNames=true,includePackage=false)
class TransporteEmpresa {

    String id
    String nombre
    String telefono1
    String telefono2
    String telefono3
    Direccion direccion

    static constraints = {
    	telefono1 nullable: true
        telefono2 nullable: true
        telefono3 nullable: true
    }

    static embedded = ['direccion']

    static mapping= {
        id generator: 'uuid'
        
    }
}
