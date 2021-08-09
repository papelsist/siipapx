package sx.logistica

import sx.security.User

class AsignacionActividad {


    String	id

    User asigno

    User termino

    User	empleado

    String	actividad

    Date	fin

    Date	inicio

    Integer	evaluacion	 = 0

    String	comentario

 

    static constraints = {
        actividad inList: ['DESCARGA','SECTOR','BARRER','SALIDA SUCURSAL']
        inicio nullable: true
        termino nullable: true
        fin nullable: true
        comentario nullable: true
    }

    static mapping ={
        id generator: 'uuid'
    }
}
