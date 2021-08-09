package sx.logistica

class IncidenciaChofer {

    String id

    Chofer chofer

    Boolean suspendido = false

    Date fecha

    String comentario

    String incidencia

    static constraints = {
        comentario nullable: true
        incidencia inList:['FALTA','RETARDOS','MAL_ASPECTO','ALCOHOLIZADO','AGRESIVO','FUERA_DE_RUTA','IRRESPONSABLE']
    }

    static mapping = {
        id generator: 'uuid'
    }
}
