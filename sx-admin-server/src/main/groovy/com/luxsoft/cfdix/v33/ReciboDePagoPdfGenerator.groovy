package com.luxsoft.cfdix.v33


import com.luxsoft.utils.ImporteALetra
import groovy.util.logging.Slf4j
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.Pagos
import lx.cfdi.v33.Pagos.Pago.DoctoRelacionado
import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType
import org.apache.commons.io.FileUtils

import sx.cfdi.Cfdi
import sx.cfdi.CfdiTimbre
import sx.cxc.Cobro


import java.text.MessageFormat

/**
 *
 */
@Slf4j
class ReciboDePagoPdfGenerator {

    static getReportData(Cobro nota) {
        Cfdi cfdi = nota.cfdi
        File xmlFile = FileUtils.toFile(cfdi.url)
        Comprobante comprobante = CfdiUtils.read(xmlFile)
        Pagos pagos = (Pagos)comprobante.complemento.any[0]
        def relacionados = pagos.pago.doctoRelacionado

        def modelData = relacionados.get(0).collect { DoctoRelacionado cc ->

            def res=[
                    'IdDocumento': cc.idDocumento,
                    'Serie' : cc.serie,
                    'Folio': cc.folio,
                    'MonedaDR': cc.monedaDR.toString(),
                    'MetodoDePagoDR': cc.metodoDePagoDR.toString(),
                    'NumParcialidad': cc.numParcialidad.toString(),
                    'ImpPagado': cc.impPagado,
                    'ImpSaldoAnt': cc.impSaldoAnt,
                    'ImpSaldoInsoluto': cc.impSaldoInsoluto,
            ]
            return res
        }

        def data = [:]

        def params = getParametros(nota, cfdi, comprobante, xmlFile)
        params['FECHA_PAGO'] = pagos.pago.get(0).fechaPago
        params['FORMA_DE_PAGO'] = pagos.pago.get(0).formaDePagoP
        params['MONEDAP'] = pagos.pago.get(0).monedaP.toString()
        params['MONTO'] = pagos.pago.get(0).monto
        params['NUM_OPERACION'] = pagos.pago.get(0).numOperacion
        params['SUBTOTAL'] = comprobante.subTotal.toString()
        // params["IMP_CON_LETRA"] = ImporteALetra.aLetra(pagos.pago.get(0).monto)
        if (nota.moneda.currencyCode == 'USD') {
            params["IMP_CON_LETRA"] = ImporteALetra.aLetraDolares(comprobante.getTotal())
        } else
            params["IMP_CON_LETRA"] = ImporteALetra.aLetra(comprobante.getTotal())
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

    static getParametros(Cobro nota, Cfdi cfdi, Comprobante comprobante, File xmlFile){
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
            params.put("TIPO_DE_COMPROBANTE", "P (Pago)")
        }
        params.FECHA = comprobante.fecha
        BigDecimal descuento = comprobante.getDescuento() ?: 0.0

        params.DESCUENTOS = descuento as String
        params.IMPORTE_BRUTO = (comprobante.getSubTotal() - descuento) as String
        params['PINT_IVA']='16 '
        params["IVA"] = (comprobante?.getImpuestos()?.getTotalImpuestosTrasladados()?: 0.0) as String
        if(comprobante?.cfdiRelacionados?.tipoRelacion){
            params["TIPO_DE_RELACION"] = comprobante.cfdiRelacionados.tipoRelacion
            String relacionados = comprobante.cfdiRelacionados.cfdiRelacionado.collect{it.UUID}.join(', ')
            params['RelacionUUID'] = relacionados

        }
       params['COMENTARIOS'] = nota.comentario
        params['COMENTARIOS'] = nota.comentario

        return params;
    }
}
