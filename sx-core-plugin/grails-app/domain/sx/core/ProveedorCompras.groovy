package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(excludes ='id,version,dateCreated,lastUpdated',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id')
class ProveedorCompras {

    String id

    Proveedor	proveedor

    String	cuentaContable = 0

    Long	descuentoF	 = 0

    Long	diasDF	 = 0

    Long	plazo	 = 0

    Boolean	fechaRevision	 = true

    Boolean	imprimirCosto	 = false

    static constraints ={
        cuentaContable nullable: true
        proveedor nullable: true
    }

    static  mapping ={
        id generator:'uuid'
    }
}
