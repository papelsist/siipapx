package sx.cfdi

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString( includes = "tipoDeComite, tipoDeProceso, contabilidad",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id, venta')
class ComplementoIneEntidad {

    String	id

    String	ambito

    String	clave

    List<Integer> contabilidad

    ComplementoIne complemento

    static constraints = {
        ambito nullable: true, inList: ['Local', 'Federal']
        clave maxSize: 10
    }

    static  mapping ={
        id generator:'uuid'
    }

    static belongsTo = [complemento: ComplementoIne]

    static hasMany =[contabilidad:Integer]

}
