package com.luxsoft.cfdix.v33

import org.apache.commons.logging.LogFactory
import org.bouncycastle.util.encoders.Base64

import sx.core.Empresa
import sx.core.Venta


import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.ObjectFactory
import lx.cfdi.v33.Comprobante

// Catalogos
import lx.cfdi.v33.CUsoCFDI
import lx.cfdi.v33.CMetodoPago
import lx.cfdi.v33.CTipoDeComprobante
import lx.cfdi.v33.CMoneda
import lx.cfdi.v33.CTipoFactor

// Utilerias
import sx.utils.MonedaUtils

/**
 * TODO: Parametrizar el regimenFiscal de
 */
class CfdiBuilder33 {

  private static final log=LogFactory.getLog(this)

  private factory = new ObjectFactory();
  private Comprobante comprobante;
  private Empresa empresa
  private Venta venta;

  private BigDecimal subTotal = 0.0
  private BigDecimal totalImpuestosTrasladados



  def build(Venta venta){
    buildComprobante(venta)
    /*
    .buildEmisor()
    .buildReceptor()
    .buildFormaDePago()
    .buildConceptos()
    .buildImpuestos()
    .buildTotales()
    .buildRelacionados()
    .buildCertificado()
    */
    return comprobante
  }
    

  def buildComprobante(Venta venta){
    log.info("Generando CFDI 3.3 para venta: ${venta.id}")
    this.empresa = Empresa.first()
    this.comprobante = factory.createComprobante()
    this.venta = venta;
    
    comprobante.version = "3.3"
    comprobante.tipoDeComprobante = CTipoDeComprobante.I
    comprobante.serie = venta.tipo
    comprobante.folio = venta.documento.toString()
    comprobante.setFecha(DateUtils.getCfdiDate(new Date()))
    comprobante.moneda =  V33CfdiUtils.getMonedaCode(venta.moneda)
    comprobante.lugarExpedicion = venta.sucursal.direccion.codigoPostal
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
    receptor.rfc = venta.cliente.rfc
    receptor.nombre = venta.cliente.nombre
    switch(venta.usoCfdi) {
      case 'G01':
          receptor.usoCFDI = CUsoCFDI.G_01
          break
      case 'G02':
          receptor.usoCFDI = CUsoCFDI.G_02
          break
      case 'G03':
          receptor.usoCFDI = CUsoCFDI.G_03
          break
      default:
          receptor.usoCFDI = CUsoCFDI.P_01
      break
    }
    comprobante.receptor = receptor
    return this
  }

  def buildFormaDePago(){
      comprobante.formaPago = '99'
      comprobante.condicionesDePago = 'Credito 30 días'
      comprobante.metodoPago = CMetodoPago.PPD
      return this
  }

  def buildConceptos(){
    /** Conceptos ***/

    if(this.venta.tipo == 'NOTA_DE_CARGO'){
        
        return buildConceptosDeNotaDeCargo()
    }
    this.totalImpuestosTrasladados = 0.0
    Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
    this.venta.partidas.each { det ->
        
      Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
      concepto.with { 
        assert det.producto.productoSat, 
        "No hay una claveProdServ definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
        assert det.producto.unidad.claveUnidadSat, 
        "No hay una claveUnidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
        assert det.producto.unidad.unidadSat, 
        "No hay una unidadSat definida para el producto ${det.producto} SE REQUIERE PARA EL CFDI 3.3"
        String desc = det.producto.descripcion
        if(det.producto.clave == 'SRV0001'){
            desc+= ' ' + venta.comentario
        }
        claveProdServ = det.producto.productoSat.claveProdServ
        noIdentificacion = det.producto.clave
        cantidad = MonedaUtils.round(det.cantidad / det.producto.unidad.factor,3)
        claveUnidad = det.producto.claveUnidadSat
        unidad = det.producto.unidad.clave
        descripcion = desc
        valorUnitario = det.precio
        importe = det.importe
        // Impuestos del concepto
        concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
        concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
        Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1 
        traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
        traslado1.base =  det.importe
        traslado1.impuesto = '002'
        traslado1.tipoFactor = CTipoFactor.TASA
        traslado1.tasaOCuota = '0.160000'
        traslado1.importe = MonedaUtils.round(det.importe * 0.16)
        if(det.impuestoTasa == 0.00){
          traslado1.tasaOCuota = '0.000000'
        }
        
        this.totalImpuestosTrasladados += traslado1.importe
        concepto.impuestos.traslados.traslado.add(traslado1)
        conceptos.concepto.add(concepto)

        def pedimento=det.embarque?.pedimento
        if(pedimento){
            Comprobante.Conceptos.Concepto.InformacionAduanera aduana = 
                factory.createComprobanteConceptosConceptoInformacionAduanera()
            aduana.numeroPedimento = pedimento.pedimento
            concepto.informacionAduanera.add(aduana)
            descripcion = "${desc} ${pedimento.fecha.text()}"
        }
        comprobante.conceptos = conceptos
      }
    }
    return this
  }

  def buildConceptosDeNotaDeCargo(){

    this.totalImpuestosTrasladados = 0.0
    Comprobante.Conceptos conceptos = factory.createComprobanteConceptos()
    
    this.venta.conceptos.each { det ->
        
      Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto()
      
      concepto.with {
        def cfdi = det.cfdi
        def desc = "${venta.comentario} Fac: ${cfdi?.folio} ${cfdi?.fecha?.text()}"
        claveProdServ = det.claveProdServ
        noIdentificacion = det.numeroDeIdentificacion
        cantidad = MonedaUtils.round(det.cantidad)
        claveUnidad = 'E48'
        unidad = det.unidad
        descripcion = desc
        valorUnitario = det.valorUnitario
        importe = det.importe
        this.subTotal += det.importe
        // Impuestos del concepto
        concepto.impuestos = factory.createComprobanteConceptosConceptoImpuestos()
        concepto.impuestos.traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados()
        Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado traslado1 
        traslado1 = factory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado()
        traslado1.base =  det.importe
        traslado1.impuesto = '002'
        traslado1.tipoFactor = CTipoFactor.TASA
        traslado1.tasaOCuota = '0.160000'
        traslado1.importe = MonedaUtils.round(det.importe * 0.16)
        this.totalImpuestosTrasladados += traslado1.importe
        concepto.impuestos.traslados.traslado.add(traslado1)
        conceptos.concepto.add(concepto)
        comprobante.conceptos = conceptos
      }
    }
    return this
  }

  def buildRelacionados(){    
    if(this.venta.tipo == 'NOTA_DE_CARGO'){
      Comprobante.CfdiRelacionados relacionados = factory.createComprobanteCfdiRelacionados()
      relacionados.tipoRelacion = '02'
      venta.conceptos.each {
          Comprobante.CfdiRelacionados.CfdiRelacionado relacionado = factory.createComprobanteCfdiRelacionadosCfdiRelacionado()
          def cfdi = it.cfdi
          relacionado.UUID = cfdi.uuid
          relacionados.cfdiRelacionado.add(relacionado) // .add(relacionado)
      }
      comprobante.cfdiRelacionados = relacionados
    }
    return this
  }

  def buildImpuestos(){
      /** Impuestos **/
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
    return this
  }

  def buildTotales(){
    if(this.venta.tipo == 'NOTA_DE_CARGO'){
        comprobante.subTotal = this.subTotal
        comprobante.total = venta.importe + this.totalImpuestosTrasladados    
    } else {
        comprobante.subTotal = venta.subtotal
        comprobante.total = venta.importe + this.totalImpuestosTrasladados    
    }
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
