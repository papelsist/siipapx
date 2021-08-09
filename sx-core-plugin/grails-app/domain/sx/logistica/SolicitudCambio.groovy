package sx.logistica

import sx.core.Sucursal
import sx.security.User

class SolicitudCambio {

    String id

    String modulo

    Sucursal sucursal

    Date fecha

    Integer folio = 0

    String tipo
    
    User usuario

    User autorizo

    User atendio

    String descripcion

    String comentario

    String comentarioAutorizacion

    String comentarioAtencion

    String documento

    String documentoDesrcipcion

    Date solicitud 

    Date autorizacion 

    Date atencion

    String estado

    String fechaDocumento

    Date dateCreated
    
    Date lastUpdated

    static constraints = {
        modulo inList:['CLIENTES','COBRANZA','INVENTARIOS','EMBARQUES','DEPOSITOS','USUARIOS','TRASLADOS','VENTAS','PEDIDOS','CAJA','COMPRAS','CREDITO']
        tipo nullable:true
        autorizo nullable:true
        atendio nullable:true
        descripcion nullable:true
        comentario nullable:true
        comentarioAutorizacion nullable:true
        comentarioAtencion nullable:true
        documento nullable:true
        documentoDesrcipcion nullable:true
        solicitud nullable:true
        autorizacion nullable:true
        atencion nullable:true
        estado inList:['PENDIENTE','ATENDIDA','AUTORIZADA','RECHAZADA','CANCELADA']
        fechaDocumento nullable:true
    }

    static mapping ={
        id generator: 'uuid'
    }
    
}
