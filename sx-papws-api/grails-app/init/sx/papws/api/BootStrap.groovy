package sx.pos.server


class BootStrap {

    def init = { servletContext ->
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"))
    }
    def destroy = {
    }
}
