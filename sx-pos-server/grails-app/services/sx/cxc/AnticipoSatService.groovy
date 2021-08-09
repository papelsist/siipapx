package sx.cxc

import groovy.util.logging.Slf4j
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import grails.gorm.transactions.Transactional

import sx.cloud.FirebaseService

@Slf4j
@Transactional
class AnticipoSatService {

    FirebaseService firebaseService

    def generarAnticipo(CuentaPorCobrar cuentaPorCobrar) {
        if(cuentaPorCobrar.tipo != 'ANT') 
            throw new RuntimeException("La factura ${cuentaPorCobrar.folio} no es de anticipo")
        log.info('Generando anticipo para CXC: {}, UUID: {} ', cuentaPorCobrar.getFolio(), cuentaPorCobrar.cfdi.uuid)
        def comprobante = cuentaPorCobrar.cfdi
        def anticipo = new AnticipoSat()
        anticipo.sucursal = cuentaPorCobrar.sucursal.nombre
        anticipo.cliente = cuentaPorCobrar.cliente.id
        anticipo.nombre = cuentaPorCobrar.cliente.nombre
        anticipo.rfc = cuentaPorCobrar.cliente.rfc
        anticipo.fecha = cuentaPorCobrar.fecha
        anticipo.cxc = cuentaPorCobrar.id
        anticipo.cxcDocumento = cuentaPorCobrar.documento
        anticipo.moneda = cuentaPorCobrar.moneda.currencyCode
        anticipo.tipoDeCambio = cuentaPorCobrar.tipoDeCambio
        anticipo.cfdi = comprobante.id
        anticipo.cfdiSerie = comprobante.serie
        anticipo.cfdiFolio = comprobante.folio
        anticipo.uuid = comprobante.uuid
        anticipo.importe = cuentaPorCobrar.importe
        anticipo.impuesto = cuentaPorCobrar.impuesto
        anticipo.total = cuentaPorCobrar.total
        anticipo.comentario = cuentaPorCobrar.comentario
        anticipo.createUser = cuentaPorCobrar.createUser
        anticipo.updateUser = cuentaPorCobrar.updateUser
        anticipo = anticipo.save failOnError: true, flush: true
        return anticipo
    }


    Map generarCobroConAnticipo(CuentaPorCobrar cxc, AnticipoSat anticipo) {
        if(cxc.saldo <= 0.0) 
            throw new RuntimeException("La factura ${cxc.folio} ya esta pagada")
        if(anticipo.disponible <= 0.0) 
            throw new RuntimeException("Anticipo: ${anticipo.folio} ya no tiene disponible")
        Cobro cobro = new Cobro([anticipo: true, anticipoSat: anticipo.id]);
        def aplicado = cxc.total <= anticipo.disponible ? cxc.total : anticipo.disponible
        def total = aplicado == cxc.total
        cobro.with {
            sucursal = cxc.sucursal
            cliente = cxc.cliente
            tipo = cxc.tipo
            fecha = new Date()
            formaDePago = 'COMPENSACION'
            moneda = cxc.moneda
            tipoDeCambio = cxc.tipoDeCambio
            importe = cxc.total <= anticipo.disponible ? cxc.total : anticipo.disponible
            referencia = anticipo.folio
            createUser = cxc.createUser
            updateUser = cxc.updateUser
            comentario = "PAGO ${total ? 'TOTAL': 'PARCIAL'} ${anticipo.folio}"
        }
        cxc.relacionados = buildRelacionados(anticipo)
        cxc.anticipo = anticipo.id
        cxc.anticipoTipo = total ? 'TOTAL' : 'PARCIAL'
        if(cxc.anticipoTipo == 'PARCIAL') {
            cxc.tipo = 'COD'
        }
        aplicar(cxc, cobro)
        cxc.save()
        cobro = cobro.save failOnError: true, flush: true
        return [cobro: cobro, cxc: cxc]
    }

    def aplicar(CuentaPorCobrar cxc, Cobro cobro) {
        def aplicacion = new AplicacionDeCobro()
        aplicacion.cuentaPorCobrar = cxc
        aplicacion.fecha = cobro.fecha
        aplicacion.importe = cobro.importe
        aplicacion.formaDePago = cobro.formaDePago
        cobro.primeraAplicacion = aplicacion.fecha
        cobro.addToAplicaciones(aplicacion)
        return cobro
    }

    def registrarDetalle(Cobro cobro, CuentaPorCobrar cxc) {
        return new AnticipoSatDet([
            sucursal: cxc.sucursal.nombre,
            fecha: cobro.fecha,
            cxc: cxc.id,
            cxcDocumento: cxc.documento,
            cxcTipo: cxc.tipo,
            moneda: cxc.moneda.currencyCode,
            tipoDeCambio: cxc.tipoDeCambio,
            cobro: cobro.id,
            importe: cobro.importe,
            comentario: cobro.comentario,
            createUser: cobro.createUser,
            updateUser: cobro.updateUser
            ])
    }


    String buildRelacionados(AnticipoSat  anticipo) {
        log.debug('Relacionando anticipo uuid: {}', anticipo.uuid)
        def list = [[tipo: '07', uuid:anticipo.uuid]]
        return new JsonBuilder(list).toString()
    }

    List parseRelacionados(String json) {
        return new JsonSlurper().parseText(json)
    }

    def copyProperties(source, target) {
        def (sProps, tProps) = [source, target]*.properties*.keySet()
        def commonProps = sProps.intersect(tProps) - ['class', 'metaClass', 'additionalMetaMethods']
        commonProps.each { target[it] = source[it] }
    }

    void updateFirebase(AnticipoSat anticipo) {
        Map changes = anticipo.toFirebase()
        firebaseService.updateCollection('anticipos', anticipo.getFolio(), anticipo.toFirebase())
    }

    void deleteFromFirebase(AnticipoSat anticipo) {
        firebaseService.deleteDocument('anticipos', anticipo.getFolio())
    }

}
