package sx.logistica

import sx.security.User

class ActividadAlmacen {

    String	id

    User asigno

    User	empleado

    String	actividad

    Date	fin

    Date	inicio

    Integer	evaluacion	 = 0

    String	comentario

    Date	date_created

    Date	last_updated

    Integer	version	 = 0

    static constraints = {
        actividad inList:['DESCARGAR','BARRER','CONTAR','LIMPIAR','ACOMODAR','COMIDA','PERMISO','OTRAS']
    }
    static mapping ={
        id generator: 'uuid'
    }
}
