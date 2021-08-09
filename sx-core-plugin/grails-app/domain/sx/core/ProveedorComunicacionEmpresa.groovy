package sx.core

class ProveedorComunicacionEmpresa {

    String	id

    Boolean	activo	 = true

    Proveedor	proveedor

    String	tipo

    String	descripcion

    String	comentario

    Long	sw2	 = 0

    static constraints = {
        descripcion nullable: true
        comentario nullable: true
        tipo  inList:['CEL','TEL','FAX','MAIL','WEB']

    }

    static mapping={
        id generator:'uuid'
    }
}
