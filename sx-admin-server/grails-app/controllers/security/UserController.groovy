package sx.security

import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasRole('ROLE_ADMIN')")
class UserController extends RestfulController {

    static responseFormats = ['json']

    UserController() {
        super(User)
    }

    def findByNip() {
        def nip = params['nip'];
        User user = User.where {nip == nip}.find();
        if (user == null) {
            notFound()
            return
        }
        respond user
    }


}
