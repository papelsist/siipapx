package sx.tesoreria

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode(includes='id')
class MovimientoDeCuenta {

    String id

    CuentaDeBanco cuenta

    String afavor

    Date fecha

    String formaDePago

    String tipo

    String referencia

    String concepto

    BigDecimal importe

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tipoDeCambio = 1.0

    String comentario

    Long sw2

    Date dateCreated

    Date lastUpdated

    String conceptoReporte
    String sucursal
    boolean porIdentificar = false

    static belongsTo = [movimientoDeTesoreria: MovimientoDeTesoreria]

    //static mappedBy = [pago: 'egreso']

    static constraints = {
        tipoDeCambio scale:6
        formaDePago maxSie: 20
        tipo maxSie: 15
        concepto maxSie:20
        importe scale:4
        referencia nullable: true
        comentario nullable: true
        concepto nullable:true,maxSize:50
        sw2 nullable:true
        movimientoDeTesoreria nullable: true
        conceptoReporte nullable: true
        sucursal nullable: true
        porIdentificar nullable: true

    }

    static mapping ={
        id generator: 'uuid'
        fecha type:'date' , index: 'MOV_CTA_IDX1'
        afavor index: 'MOV_CTA_IDX2'
        formaDePago index: 'MOV_CTA_IDX3'
    }



    //static belongsTo = [cobro: Cobro,comision:Comision,traspaso:Traspaso]
    //static belongsTo =[Traspaso,Comision,PagoProveedor,CompraDeMoneda]

    String toString(){
        return "${cuenta}  (${fecha.format('dd/MM/yyyy')}) ${importe}"
    }
}
