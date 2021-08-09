package com.luxsoft.cfdix.v33

import com.luxsoft.utils.ImporteALetra
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType
import org.apache.commons.io.FileUtils

import sx.cfdi.Cfdi
import sx.cfdi.CfdiTimbre
import sx.cxc.NotaDeCargo


import java.text.MessageFormat

/**
 *
 */
class NotaDeCargoPdfGenerator {

    static getReportData(NotaDeCargo nota){
        assert nota.cfdi
        Cfdi cfdi = nota.cfdi
        File xmlFile = FileUtils.toFile(cfdi.url)
        Comprobante comprobante = CfdiUtils.read(xmlFile)

        def conceptos = comprobante.conceptos.concepto

        def index = 0
        def modelData=conceptos.collect { cc ->
            def traslado = cc.impuestos.traslados.traslado[0]
            def res=[
                    'cantidad' : cc.getCantidad(),
                    'NoIdentificacion' : cc.noIdentificacion,
                    'descripcion' : cc.descripcion,
                    'unidad': cc.unidad,
                    'ValorUnitario':cc.valorUnitario,
                    'Importe':cc.importe,
                    'ClaveProdServ': cc.claveProdServ,
                    'ClaveUnidad': cc.claveUnidad,
                    'Descuento': '0.0',
                    'Impuesto': traslado.impuesto.toString(),
                    'TasaOCuota': traslado.tasaOCuota.toString(),
                    'TipoFactor': traslado.tipoFactor.value().toString(),
                    'Base': traslado.base,
                    'TrasladoImporte': traslado.importe
            ]
            return res
        }
        def params = getParametros(nota,cfdi, comprobante, xmlFile)
        def data = [:]
        data['CONCEPTOS'] = modelData
        data['PARAMETROS'] = params

        return data
    }

    public static  generarQR(Cfdi cfdi) {
        String pattern="?re=${0}&rr={1}&tt={2,number,##########.######}&id,{3}"
        String qq=MessageFormat.format(pattern, cfdi.emisorRfc,cfdi.receptorRfc,cfdi.total,cfdi.uuid)
        File file=QRCode.from(qq).to(ImageType.GIF).withSize(250, 250).file()
        return file.toURI().toURL()

    }

    static getParametros(NotaDeCargo nota, Cfdi cfdi, Comprobante comprobante, File xmlFile){
        def params=[:]
        params["VERSION"] = comprobante.version
        params["SERIE"] = comprobante.getSerie()
        params["FOLIO"] = comprobante.getFolio()
        params["NUM_CERTIFICADO"] = comprobante.getNoCertificado()
        params["SELLO_DIGITAL"] = comprobante.getSello()
        params["RECEPTOR_NOMBRE"] = comprobante.getReceptor().getNombre()
        params["RECEPTOR_RFC"] = comprobante.getReceptor().getRfc()
        params["IMPORTE"] = comprobante.getSubTotal()
        params["TOTAL"] = comprobante.getTotal().toString()
        params["RECEPTOR_DIRECCION"] = 'ND'
        params["METODO_PAGO"] = comprobante.metodoPago.toString()
        params["FORMA_PAGO"] = comprobante.formaPago
        // params["IMP_CON_LETRA"] = ImporteALetra.aLetra(comprobante.getTotal());

        if (nota.moneda.currencyCode == 'USD') {
            params["IMP_CON_LETRA"] = ImporteALetra.aLetraDolares(comprobante.getTotal())
        } else
            params["IMP_CON_LETRA"] = ImporteALetra.aLetra(comprobante.getTotal());

        params['FORMA_DE_PAGO']=comprobante.formaPago
        params['UsoCFDI'] = comprobante.receptor.usoCFDI.value().toString()
        params['Moneda'] = comprobante.moneda.value().toString()
        def emisor=comprobante.getEmisor()
        params["EMISOR_NOMBRE"] = emisor.getNombre()
        params["EMISOR_RFC"] =  emisor.getRfc()
        params["EMISOR_DIRECCION"] = ' '
        params["REGIMEN"] = comprobante.emisor.regimenFiscal
        params["LUGAR_EXPEDICION"] = comprobante.lugarExpedicion


        if(cfdi.uuid!=null){
            def img = generarQR(cfdi)
            params.put("QR_CODE",img);
            CfdiTimbre timbre = new CfdiTimbre(xmlFile.bytes)
            params.put("FECHA_TIMBRADO", timbre.fechaTimbrado);
            params.put("FOLIO_FISCAL", timbre.uuid);
            params.put("SELLO_DIGITAL_SAT", timbre.selloSAT);
            params.put("CERTIFICADO_SAT", timbre.noCertificadoSAT);
            params.put("CADENA_ORIGINAL_SAT", timbre.cadenaOriginal());
            params.put("RfcProvCertif", timbre.rfcProvCertif)
            params.put("TIPO_DE_COMPROBANTE", "I (Ingreso)")
        }
        params.FECHA = comprobante.fecha
        BigDecimal descuento = comprobante.getDescuento() ?: 0.0

        params.DESCUENTOS = descuento as String
        params.IMPORTE_BRUTO = (comprobante.getSubTotal() - descuento) as String
        params['PINT_IVA']='16 '
        params["IVA"] = (comprobante?.getImpuestos()?.getTotalImpuestosTrasladados()?: 0.0) as String

        if(nota.tipo != 'CHE'){
            String relacionados = comprobante?.cfdiRelacionados?.cfdiRelacionado?.collect{it.UUID}?.join(', ')
            //println 'RELACIONADOS: '+ relacionados
            if(relacionados) {
                params['RelacionUUID'] = relacionados
                params['COMENTARIOS'] = nota.comentario
            }
        }
        params['COMENTARIOS'] = nota.comentario
        return params;
    }
}
