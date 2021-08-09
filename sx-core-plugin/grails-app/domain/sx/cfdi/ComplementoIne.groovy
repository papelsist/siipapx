package sx.cfdi

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Venta


@ToString( includes = "tipoDeComite, tipoDeProceso, contabilidad",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id, venta')
class ComplementoIne {

    String id

    String tipoDeComite

    String tipoDeProceso

    Long contabilidad

    Venta venta

    List<ComplementoIneEntidad> partidas = [];

    static constraints = {
        tipoDeComite nullable: true, inList: ['Ejecutivo Nacional','Ejecutivo Estatal','Directivo Estatal']
        tipoDeProceso inList: ['Ordinario', 'Precampaña', 'Campaña']
        contabilidad nullable: true
    }

    static mapping = {
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
    }


    static hasMany =[partidas:ComplementoIneEntidad]


}
