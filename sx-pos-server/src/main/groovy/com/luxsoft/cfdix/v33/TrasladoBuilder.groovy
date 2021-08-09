package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.*
// import org.apache.commons.logging.LogFactory
import org.bouncycastle.util.encoders.Base64
import sx.core.Empresa
import sx.inventario.Traslado
import sx.inventario.TrasladoDet


// Catalogos
import sx.utils.MonedaUtils

//
/**
 * TODO: Parametrizar el regimenFiscal de
 */
@Slf4j
class TrasladoBuilder {


    // private static final log=LogFactory.getLog(this)

    CfdiSellador33 sellador

    private factory = new ObjectFactory();
    private Comprobante comprobante;
    private Empresa empresa

    private Traslado tps

    private BigDecimal subTotal = 0.0
    private BigDecimal totalImpuestosTrasladados


    def build(Traslado tps){

        this.tps = tps
        this.empresa = Empresa.first()
        // assert empresa, 'La empresa no esta registrada...'
        buildComprobante()
                .buildFormaDePago()
                .buildEmisor()
                .buildReceptor()
                .buildConceptos()
                .buildImpuestos()
                .buildTotales()
                .buildCertificado()
        comprobante = sellador.sellar(comprobante, empresa)
        return comprobante
    }


    def buildComprobante(){
        log.info("Generando CFDI 3.3 para {} - {} ",this.tps.tipo, this.tps.documento)
        this.comprobante = factory.createComprobante()
        comprobante.version = "3.3"
        comprobante.tipoDeComprobante = CTipoDeComprobante.T
        comprobante.serie = "${tps.sucursal.nombre.substring(0,2)}_TPS"
        comprobante.folio = tps.documento.toString()
        comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
        comprobante.moneda =  CMoneda.MXN
        comprobante.lugarExpedicion = tps.sucursal.direccion.codigoPostal
        return this
    }

    def buildEmisor(){
        /**** Emisor ****/
        Comprobante.Emisor emisor = factory.createComprobanteEmisor()
        emisor.rfc = empresa.rfc
        emisor.nombre = empresa.nombre
        emisor.regimenFiscal = empresa.regimenClaveSat ?:'601'
        comprobante.emisor = emisor
        return this
    }

    def buildReceptor(){
        /** Receptor ***/
        Comprobante.Receptor receptor = factory.createComprobanteReceptor()
        receptor.rfc = 'XAXX010101000'
        receptor.nombre = 'PUBLICO EN GENERAL'
        receptor.usoCFDI = CUsoCFDI.P_01
        comprobante.receptor = receptor
        return this
    }

    /**
     *  FIX Para CRE, CON y COD
     */
    def buildFormaDePago(){
        // comprobante.formaPago = '99'
        // comprobante.metodoPago = CMetodoPago.PPD
        return this
    }

    def buildConceptos(){
        /** Conceptos ***/
        this.totalImpuestosTrasladados = 0.0
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
        this.tps.partidas.each { TrasladoDet det ->

            Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
            concepto.with {
                assert det.producto.productoSat,
                        "No hay una claveProdServ definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                assert det.producto.unidadSat.claveUnidadSat,
                        "No hay una claveUnidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
                def factor = det.producto.unidad == 'MIL' ? 1000 : 1
                String desc = det.producto.descripcion
                claveProdServ = det.producto.productoSat.claveProdServ
                noIdentificacion = det.producto.clave
                cantidad = MonedaUtils.round(det.cantidad.abs() / factor,3)
                claveUnidad = det.producto.unidadSat.claveUnidadSat
                unidad = det.producto.unidad
                descripcion = 'Traslado de mercancias ' + desc
                valorUnitario = MonedaUtils.round(0, 2)
                importe = MonedaUtils.round(0, 2)
                // descuento = MonedaUtils.round(0, 2)

                // Impuestos del concepto
                /*
                concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
                concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
                Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1
                traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
                traslado1.base =  det.subtotal
                traslado1.impuesto = '002'
                traslado1.tipoFactor = CTipoFactor.TASA
                traslado1.tasaOCuota = '0.160000'
                traslado1.importe = det.impuesto
                this.totalImpuestosTrasladados += traslado1.importe
                concepto.impuestos.traslados.traslado.add(traslado1)
                */

                conceptos.concepto.add(concepto)
                comprobante.conceptos = conceptos
            }
        }
        return this
    }

    def buildImpuestos(){
        /** Impuestos **/
        /*
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos()
        impuestos.setTotalImpuestosTrasladados(this.totalImpuestosTrasladados)
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados()
        Comprobante.Impuestos.Traslados.Traslado traslado = factory.createComprobanteImpuestosTrasladosTraslado()
        traslado.impuesto = '002'
        traslado.tipoFactor = CTipoFactor.TASA
        traslado.tasaOCuota = '0.160000'
        //traslado.importe = venta.impuestos
        traslado.importe = this.totalImpuestosTrasladados
        traslados.traslado.add(traslado)
        impuestos.traslados = traslados
        comprobante.setImpuestos(impuestos)
        */
        return this
    }

    def buildTotales(){
        comprobante.subTotal = 0.0 //.setScale(2, RoundingMode.CEILING)
        comprobante.total = 0.0

        return this
    }

    def buildCertificado(){
        comprobante.setNoCertificado(empresa.numeroDeCertificado)
        byte[] encodedCert=Base64.encode(empresa.getCertificado().getEncoded())
        comprobante.setCertificado(new String(encodedCert))
        return this

    }

    Comprobante getComprobante(){
        return this.comprobante
    }

}
