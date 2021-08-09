package sx.pos.server

import grails.compiler.GrailsCompileStatic
import sx.core.AppConfig


@GrailsCompileStatic
class AppConfigInterceptor {

    private AppConfig appConfig

    public AppConfigInterceptor(){
        match controller: 'pedido'
    }

    boolean before() {
        // log.debug('Anexando sucursal {}', getAppConfig().sucursal)
        params.sucursal = getAppConfig().sucursal
        true
    }

    public AppConfig getAppConfig(){
        if(!appConfig) {
            this.appConfig = AppConfig.first()
        }
        return this.appConfig
    }


}
