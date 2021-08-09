package sx.pos.server

import org.bouncycastle.jce.provider.BouncyCastleProvider

class BootStrap {

    def init = { servletContext ->
        java.security.Security.addProvider(new BouncyCastleProvider())
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"))
    }
    def destroy = {
    }
}
