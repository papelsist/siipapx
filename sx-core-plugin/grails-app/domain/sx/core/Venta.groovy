package sx.core

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.cfdi.ComplementoIne
import sx.cxc.CuentaPorCobrar
import sx.logistica.CondicionDeEnvio

@GrailsCompileStatic
@ToString( includes = "sucursal,documento,fecha,total",includeNames=true,includePackage=false)
@EqualsAndHashCode(includes = 'id')
class Venta {

    String id

    Cliente cliente

    String nombre

    Vendedor vendedor

    Sucursal sucursal

    Sucursal sucursalVenta

    String tipo

    Long documento = 0

    BigDecimal importe  = 0

    BigDecimal descuento = 0

    BigDecimal descuentoImporte = 0

    BigDecimal subtotal = 0

    BigDecimal impuesto = 0

    BigDecimal total = 0

    BigDecimal descuentoOriginal = 0

    BigDecimal cargosPorManiobra = 0

    BigDecimal comisionTarjeta = 0

    BigDecimal comisionTarjetaImporte = 0

    BigDecimal corteImporte = 0

    String  formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal  tipoDeCambio = 1

    BigDecimal  kilos = 0

    Date puesto

    Date facturar

    String facturarUsuario

    Boolean vale = false

    Sucursal sucursalVale

    String  clasificacionVale = 'SIN_VALE'

    Date    impreso

    String  comprador

    String  atencion

    Boolean parcial = false

    String  comentario

    String  sw2

    Date fecha

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    CuentaPorCobrar cuentaPorCobrar

    CondicionDeEnvio envio

    Boolean cod = false;

    String cfdiMail

    String usoDeCfdi

    List<VentaDet> partidas = []

    String folio

    Boolean sinExistencia = false

    Socio socio

    Boolean chequePostFechado = false;

    Boolean ventaIne = false;

    Boolean noFacturable = false;

    Boolean surtido = false

    Boolean callcenter = false

    Long callcenterVersion = 1

    static constraints = {
        nombre nullable: true
        tipo  inList:['CON','COD','CRE','PSF','INE','OTR','ACF','ANT','AND']
        documento maxSize: 20
        puesto nullable: true
        tipoDeCambio(scale:6)
        impreso nullable:true
        atencion inList:['MOSTRADOR','TELEFONICA','ND']
        clasificacionVale nullable:true,maxSize:30
        comentario nullable:true
        facturar nullable:true
        comprador nullable:true
        sucursalVenta nullable:true
        cuentaPorCobrar nullable: true
        sucursalVale nullable:true
        clasificacionVale inList: ['SIN_VALE','ENVIA_SUCURSAL','PASA_CAMIONETA','RECOGE_CLIENTE','EXISTENCIA_VENTA','VALE']
        createUser nullable:true, maxSize: 100
        updateUser nullable:true, maxSize: 100
        sw2 nullable:true
        cfdiMail nullable: true
        usoDeCfdi nullable: true, maxSize:3
        envio nullable: true
        importe scale: 2
        sinExistencia nullable: true
        socio nullable: true
        chequePostFechado nullable: true
        facturarUsuario nullable: true
        ventaIne nullable: true
        complementoIne nullable: true
        noFacturable nullable: true
        callcenterVersion nullable: true
    }

    static mapping = {
        partidas cascade: "all-delete-orphan"
        id generator:'uuid'
        fecha type:'date' ,index: 'VENTA_IDX1'
        cliente index: 'VENTA_IDX3'
    }

    static hasMany =[partidas:VentaDet]

    static hasOne = [envio: CondicionDeEnvio, complementoIne: ComplementoIne]

    static transients = ['folio']


    String getFolio() {
        return "${tipo} - ${documento}"
    }

    String statusInfo() {
        if(cuentaPorCobrar)
            return 'FAC: ' + cuentaPorCobrar.getFolio()
        else
            return 'PED: ' + getFolio()
    }

}
