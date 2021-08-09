package sx.sat


import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes='claveUnidadSat')
class UnidadSat {

  String unidadSat
  String claveUnidadSat


  static constraints = {
    unidadSat nullable: true
    claveUnidadSat nullable:true
  }

  String toString(){
    return "${claveUnidadSat} - ${claveUnidadSat}"
  }
}
