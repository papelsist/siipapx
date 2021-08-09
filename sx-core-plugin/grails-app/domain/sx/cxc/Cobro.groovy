package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.Cfdi
import sx.core.Cliente
import sx.core.Sucursal

@ToString(includes = "cliente,fecha,sucursal,formaDePago,importe",includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = 'id,fecha,total')
class  Cobro {

    String id

    Cliente cliente

    Sucursal sucursal

    String tipo

    Date fecha

    String formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tipoDeCambio = 1.0

    BigDecimal importe

    String referencia

    Date primeraAplicacion

    Boolean anticipo = false

    Boolean enviado = false

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    String sw2

    List<AplicacionDeCobro> aplicaciones = []

    BigDecimal aplicado = 0

    BigDecimal disponible = 0

    BigDecimal saldo = 0.0

    BigDecimal diferencia = 0.0

    Date diferenciaFecha

    String comentario

    List<CuentaPorCobrar> pendientesDeAplicar = []

    Date fechaDeAplicacion

    String ingreso

    Cfdi cfdi

    String anticipoSat

    static hasOne = [cheque: CobroCheque, deposito: CobroDeposito, transferencia: CobroTransferencia,tarjeta: CobroTarjeta]

    static hasMany =[aplicaciones: AplicacionDeCobro]

    static constraints = {
        tipo inList:['COD','CON','CRE','CHE','JUR']
        referencia nullable:true
        sw2 nullable:true, unique:true
        dateCreated nullable: true
        lastUpdated nullable: true
        createUser nullable: true
        updateUser nullable: true
        cheque nullable: true
        deposito nullable: true
        transferencia nullable: true
        tarjeta nullable: true
        primeraAplicacion nullable: true
        diferenciaFecha nullable: true
        diferencia nullable: true
        comentario nullable: true
        tipoDeCambio scale:6
        cfdi nullable: true
        anticipoSat nullable: true
    }

    static mapping={
        id generator:'uuid'
        fecha type:'date' ,index: 'COBRO_IDX1'
        primeraAplicacion type: 'date'
        cliente index: 'COBRO_IDX2'
        formaDePago index: 'COBRO_IDX3'
        aplicaciones cascade: "all-delete-orphan"
        aplicado formula:'(select COALESCE(sum(x.importe * x.tipo_de_cambio),0) from aplicacion_de_cobro x where x.cobro_id=id)'
        saldo formula:'importe - diferencia - (select COALESCE(sum(x.importe * x.tipo_de_cambio),0) from aplicacion_de_cobro x where x.cobro_id=id)'
        diferenciaFecha type: 'date'
    }

    static transients = ['disponible', 'pendientesDeAplicar', 'fechaDeAplicacion', 'ingreso']

    BigDecimal getDisponible(){
        return this.importe - this.aplicado - this.diferencia
    }

    def getIngreso() {
        if (deposito) {
            return deposito.ingreso?.id
        }else  if (transferencia) {
            return transferencia.ingreso?.id
        }
        return null;
    }

}
