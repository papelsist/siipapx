package sx.logistica

import sx.core.Cliente
import sx.core.Venta

class Envio {

    String	id

    Embarque embarque

    Cliente cliente

    String	origen

    String	entidad

    BigDecimal	porCobrar	 = 0

    Integer	paquetes	 = 0

    BigDecimal	kilos	 = 0

    Boolean	parcial	 = false

    Date	arribo

    Date	recepcion

    String	recibio

    String	comentario

    String	documento

    Date	fechaDocumento

    BigDecimal	totalDocumento	 = 0

    String	nombre

    String	tipoDocumento

    BigDecimal	arriboLatitud	 = 0

    BigDecimal	arriboLongitud	 = 0

    BigDecimal	recepcionLatitud	 = 0

    BigDecimal	recepcionLongitud	 = 0

    String	area

    String	formaPago

    Boolean	entregado	 = false

    String	motivo

    Boolean	completo	 = false

    Boolean	matratado	 = false

    Boolean	impreso	 = false

    Boolean	cortado	 = false

    String	reportoNombre

    String	reportoPuesto

    BigDecimal valor = 0.0

    List partidas = []

    Date dateCreated

    Date lastUpdated

    Date salida
    
    String callcenter

    Long callcenterVersion = 1 
    

    static  hasMany= [partidas : EnvioDet]

    static belongsTo = [embarque: Embarque]

    static mapping ={
        id generator:'uuid'
        partidas cascade: 'all-delete-orphan'
    }

    static constraints = {
        area nullable:true //inList:['COMPRAS','ALMACEN','MERCANCIAS','ENCARGADO','DUEÑO','OTRA']
        formaPago nullable:true
        motivo nullable: true //inList: ['CERRADO','SALIO_A_COMER','NO_PAGO','DIRECCION_INCORRECTA','ERROR EN MOSTRADOR']
        reportoPuesto nullable: true //inList: ['ENCARGADO_EMBARQUES','ENCARGADO_SUCURSAL','MOSTRADOR',]
        reportoNombre nullable: true
        arribo nullable: true
        recepcion nullable: true
        recibio nullable: true
        comentario nullable: true
        cliente nullable: true
        salida nullable: true
        callcenter nullable: true
        callcenterVersion nullable: true
    }

    def beforeDelete() {
        if( entidad == 'VENTA') {
            CondicionDeEnvio.withNewSession {
                CondicionDeEnvio envio = CondicionDeEnvio.where{venta.id == origen}.find()
                if(envio) {
                    envio.asignado = null;
                    envio.save flush: true
                }
            }
        }
    }

    def beforeUpdate() {
        /*
       if(!partidas) {
           this.acualizarValorDeOrigen()
       } else {
       }
       */
    }

    def acualizarValorDeOrigen() {
        /*
        if (entidad == 'VENTA') {
            Venta venta = Venta.get(envio.origen)
            this.valor = venta.subtotal
        }
        */
    }

}
