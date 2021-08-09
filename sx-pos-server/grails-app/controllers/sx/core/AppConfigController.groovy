package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class AppConfigController extends RestfulController{

    static responseFormats = ['json']

    public AppConfigController(){
        super(AppConfig)
    }

    @Override
    Object show() {
      respond AppConfig.first()
    }
}
