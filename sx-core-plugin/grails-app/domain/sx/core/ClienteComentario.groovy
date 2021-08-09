package sx.core

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes = 'comentario,tipo,fecha,activo',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='comentario, tipo, fecha')
class ClienteComentario {

  String	id

  Date fecha

  String	comentario

  String	tipo

  boolean activo = true

  String createUser
  String updateUser

  Date dateCreated
  Date lastUpdated

  static belongsTo = [cliente: Cliente]

  static constraints = {
    createUser nullable: true
    updateUser nullable: true
  }

  static mapping = {
    id generator:'uuid'
    fecha type: 'date'
    // table: 'CLIENTE_COMENTARIOS'
  }
}
