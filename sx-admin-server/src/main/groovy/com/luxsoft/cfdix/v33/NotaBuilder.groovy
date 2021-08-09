package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import org.bouncycastle.util.encoders.Base64

import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CTipoFactor
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.ObjectFactory
import sx.cxc.NotaDeCreditoDet
import sx.utils.MonedaUtils
import sx.core.Empresa
import sx.core.VentaDet
import sx.cxc.CuentaPorCobrar
import sx.cxc.NotaDeCredito
import sx.inventario.DevolucionDeVenta
import sx.inventario.DevolucionDeVentaDet


@Slf4j
class NotaBuilder {

    private factory = new ObjectFactory()
    private Comprobante comprobante
    private Empresa empresa

    private NotaDeCredito nota
    private DevolucionDeVenta rmd

    private BigDecimal subTotalAcumulado = 0.0
    private BigDecimal descuentoAcumulado = 0.0
    private BigDecimal totalImpuestosTrasladados = 0.0


    CfdiSellador33 sellador

    def build(NotaDeCredito nota){
        this.rmd = null
        this.nota = nota
        this.empresa = Empresa.first()
        subTotalAcumulado = 0.0
        descuentoAcumulado = 0.0
        if (nota.tipo.startsWith('DEV')){
            rmd = DevolucionDeVenta.where{ cobro == this.nota.cobro}.find()
            assert rmd, 'No existe el RMD '
        }
        buildComprobante()
            .buildEmisor()
            .buildReceptor()
            .buildFormaDePago()
            .buildConceptos()
            .buildImpuestos()
            .buildTotales()
            .buildCertificado()
            .buildRelacionados()
        comprobante = sellador.sellar(comprobante, empresa)
        return comprobante
    }
    def buildComprobante(){
        log.info("Generando CFDI 3.3 para Nota de credito {} {} - {} ", nota.tipo, nota.serie, nota.folio)
        this.comprobante = factory.createComprobante()
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.E
        comprobante.serie = nota.serie
        comprobante.folio = nota.folio.toString()
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda =  V33CfdiUtils.getMonedaCode(nota.moneda)
        if(nota.moneda != MonedaUtils.PESOS){
            comprobante.tipoCambio = nota.tc
        }
        comprobante.lugarExpedicion = empresa.direccion.codigoPostal
        return this
    }

    def buildEmisor(){
        Comprobante.Emisor emisor = factory.createComprobanteEmisor()
        emisor.rfc = empresa.rfc
        emisor.nombre = empresa.nombre
        emisor.regimenFiscal = empresa.regimenClaveSat ?:'601'
        comprobante.emisor = emisor
        return this
    }

    def buildReceptor(){
        Comprobante.Receptor receptor = factory.createComprobanteReceptor()
        receptor.rfc = nota.cliente.rfc
        receptor.nombre = nota.cliente.nombre
        receptor.usoCFDI = CUsoCFDI.G_02
        comprobante.receptor = receptor
        return this
    }

    def buildFormaDePago() {
        comprobante.metodoPago = CMetodoPago.PUE
        if (nota.tipoCartera == 'CRE') {
            if(nota.formaDePago == null)
                comprobante.formaPago = '99'
            else {
                comprobante.formaPago = nota.formaDePago
            }
        } else {
            if (this.nota.tipo.startsWith('DEV')) {
                buildFormaDePagoDevolucionContado()
            } else {
                buildFormaDePagoBonificacionContado()

            }
        }
        return this
    }

    def buildFormaDePagoDevolucionContado(){
        def formaDePago = this.rmd.venta.formaDePago
        comprobante.formaPago = getFormaDePago(formaDePago)
    }

    def buildFormaDePagoBonificacionContado() {
        NotaDeCreditoDet found = this.nota.partidas.max {NotaDeCreditoDet it -> it.cuentaPorCobrar.total}
        log.debug('Venta origen de mayor valor: {}', found.cuentaPorCobrar.folio);
        String formaDePago = found.cuentaPorCobrar.formaDePago
        comprobante.formaPago = getFormaDePago(formaDePago)
    }

    private getFormaDePago(String formaDePago) {
        switch (formaDePago) {
            case 'EFECTIVO':
            case 'DEPOSITO_EFECTIVO':
                return '01'
            case 'CHEQUE':
            case 'DEPOSITO_CHEQUE':
                return  '02'
            case 'TRANSFERENCIA':
                return '03'
            case 'TARJETA_CREDITO':
                return  '04'
            case 'TARJETA_DEBITO':
                return  '28'
            case 'BONIFICACION':
            case 'DEVOLUCION':
                return '17'
            default:
                return '99'
        }

    }

    def buildConceptos() {
        if (this.nota.tipo.startsWith('DEV')) {
            buildConceptosDevolucion()
        } else {
            buildConceptosBonoificacion()
        }
    }

    def buildConceptosDevolucion(){
        /** Conceptos ***/

        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        this.rmd.partidas.each { DevolucionDeVentaDet item ->
            log.info('RmdDet: {}', item.id)
            VentaDet det = item.ventaDet
            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
            def factor = det.producto.unidad == 'MIL' ? 1000 : 1

            def importe = (item.cantidad/factor * det.precio)
            importe = MonedaUtils.round(importe)

            def descuento = (det.descuento / 100) * importe
            descuento = MonedaUtils.round(descuento)

            def subTot =  importe - descuento

            def impuesto = subTot * MonedaUtils.IVA
            impuesto = MonedaUtils.round(impuesto)
            log.debug("Importe: {}, Descuento: {} SubTotal: {}", importe, descuento, importe)
            // this.descuentoAcumulado = this.descuentoAcumulado + descuento
            if (descuento)
                concepto.descuento = descuento
            concepto.claveProdServ = '84111506'
            concepto.claveUnidad = 'ACT'
            concepto.noIdentificacion = det.producto.clave
            concepto.cantidad = MonedaUtils.round(item.cantidad / factor,3)
            concepto.unidad = det.producto.unidad
            concepto.descripcion = det.producto.descripcion
            concepto.valorUnitario = MonedaUtils.round(det.precio, 2)
            concepto.importe = importe

            concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
            concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()

            Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
            traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
            traslado1.base =  subTot
            traslado1.impuesto = '002'
            traslado1.tipoFactor = CTipoFactor.TASA
            traslado1.tasaOCuota = '0.160000'
            traslado1.importe = impuesto


            concepto.impuestos.traslados.traslado.add(traslado1)
            conceptos.concepto.add(concepto)

            // Acumulados
            this.totalImpuestosTrasladados += traslado1.importe
            this.subTotalAcumulado = this.subTotalAcumulado + importe
            this.descuentoAcumulado = this.descuentoAcumulado + descuento

        }
        comprobante.conceptos = conceptos
        return this
    }

    def buildConceptosBonoificacion(){
        /** Conceptos ***/
        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()

        this.nota.partidas.each { NotaDeCreditoDet item ->

            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()

            def importe = MonedaUtils.calcularImporteDelTotal(item.importe)

            def impuesto = importe * MonedaUtils.IVA
            impuesto = MonedaUtils.round(impuesto)

            concepto.claveProdServ = '84111506'
            concepto.claveUnidad = 'ACT'
            concepto.noIdentificacion = 'BONIFICACION'
            concepto.cantidad = 1
            concepto.unidad = 'ACT'
            concepto.descripcion = "Bonificación de: ${item.tipoDeDocumento} - ${item.documento}"
            concepto.valorUnitario = importe
            concepto.importe = importe

            concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
            concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()

            Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
            traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
            traslado1.base =  importe
            traslado1.impuesto = '002'
            traslado1.tipoFactor = CTipoFactor.TASA
            traslado1.tasaOCuota = '0.160000'
            traslado1.importe = impuesto


            concepto.impuestos.traslados.traslado.add(traslado1)
            conceptos.concepto.add(concepto)

            // Acumulados
            this.totalImpuestosTrasladados += traslado1.importe
            this.subTotalAcumulado = this.subTotalAcumulado + importe
            this.descuentoAcumulado = 0

        }

        comprobante.conceptos = conceptos
        return this
    }

    def buildImpuestos(){
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        impuestos.setTotalImpuestosTrasladados(MonedaUtils.round(this.totalImpuestosTrasladados))
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'

        traslado.importe = MonedaUtils.round(this.totalImpuestosTrasladados)
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        return this
    }

    def buildTotales(){
        if(this.descuentoAcumulado > 0) {
            comprobante.descuento = this.descuentoAcumulado
        }
        comprobante.subTotal = this.subTotalAcumulado
        comprobante.total = comprobante.subTotal - this.descuentoAcumulado + this.totalImpuestosTrasladados

        return this
    }

    def buildRelacionados() {
        Comprobante.CfdiRelacionados relacionados = factory.createComprobanteCfdiRelacionados()
        relacionados.tipoRelacion = '01'
        if (this.rmd) {
            relacionados.tipoRelacion = '03'
            Comprobante.CfdiRelacionados.CfdiRelacionado relacionado = factory.createComprobanteCfdiRelacionadosCfdiRelacionado()
            assert rmd.venta.cuentaPorCobrar, 'RMD sin CxC timbrada'
            def cxc = rmd.venta.cuentaPorCobrar
            def uuid = cxc.uuid
            if(uuid == null) {
                if (cxc.cfdi) {
                    uuid = cxc.cfdi.uuid
                }
            }
            assert uuid, "Cuenta por cobrar ${cxc.tipoDocumento} - ${cxc.documento} sin UUID"
            relacionado.UUID = uuid
            relacionados.cfdiRelacionado.add(relacionado)

        } else {
            relacionados.tipoRelacion = '01'
            this.nota.partidas.each { NotaDeCreditoDet det ->

                Comprobante.CfdiRelacionados.CfdiRelacionado relacionado = factory.createComprobanteCfdiRelacionadosCfdiRelacionado()
                def cxc = det.cuentaPorCobrar
                def uuid = cxc.cfdi.uuid
                assert uuid, 'No existe UUID origen para la cxc :' + cxc.id
                relacionado.UUID = uuid
                relacionados.cfdiRelacionado.add(relacionado)
            }
        }
        comprobante.cfdiRelacionados = relacionados
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }
}
