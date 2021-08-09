package sx.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_ADMIN')")
class RoleController extends RestfulController {
  
  static responseFormats = ['json']
  
  RoleController() {
      super(Role)
  }
}
