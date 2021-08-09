package com.luxsoft.cfdix.v33

import com.luxsoft.utils.ImporteALetra
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType
import org.apache.commons.io.FileUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiTimbre
import sx.inventario.Traslado

import java.text.MessageFormat
import java.text.SimpleDateFormat

class TrasladoPdfGenerator {

    final static SimpleDateFormat CFDI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    static getReportData(Traslado tps){
        Cfdi cfdi = tps.cfdi
        File xmlFile = FileUtils.toFile(tps.cfdi.url)
        Comprobante comprobante = CfdiUtils.read(xmlFile)

        def conceptos = comprobante.conceptos.concepto

        def index = 0
        def modelData=conceptos.collect { cc ->
            def res=[
                    'cantidad' : cc.getCantidad(),
                    'NoIdentificacion' : cc.noIdentificacion,
                    'descripcion' : cc.descripcion,
                    'unidad': cc.unidad,
                    'ValorUnitario':cc.valorUnitario,
                    'Importe':cc.importe,
                    'ClaveProdServ': cc.claveProdServ,
                    'ClaveUnidad': cc.claveUnidad,
            ]
            return res
        }
        def params = getParametros(tps,cfdi, comprobante, xmlFile)
        def data = [:]
        data['CONCEPTOS'] = modelData
        data['PARAMETROS'] = params
        params.SOL = tps.solicitudDeTraslado.documento.toString()
        return data
    }

    static getParametros(Traslado tps, Cfdi cfdi, Comprobante comprobante, File xmlFile){
        def params=[:]
        params["VERSION"] = comprobante.version
        params["SERIE"] = comprobante.getSerie()
        params["FOLIO"] = comprobante.getFolio()
        params["NUM_CERTIFICADO"] = comprobante.getNoCertificado()
        params["SELLO_DIGITAL"] = comprobante.getSello()
        params["RECEPTOR_NOMBRE"] = comprobante.getReceptor().getNombre()
        params["RECEPTOR_RFC"] = comprobante.getReceptor().getRfc()
        params["IMPORTE"] = comprobante.getSubTotal()
        params["TOTAL"] = comprobante.getTotal()
        params["RECEPTOR_DIRECCION"] = 'ND'
        params["METODO_PAGO"] = comprobante.metodoPago.toString()
        params["FORMA_PAGO"] = comprobante.formaPago
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
            params.put("TIPO_DE_COMPROBANTE", "T (Traslado)")
        }
        params.FECHA = comprobante.fecha
        // Adiconales
        params.SOLICITO = tps.solicitudDeTraslado.sucursalSolicita.nombre
        params.SUCURSAL = tps.sucursal.nombre
        params.ELABORO = tps.updateUser ?: 'PENDIENTE'
        params.KILOS = tps.kilos
        params.CHOFER = tps.chofer.nombre
        params.SUPERVISOR = 'PENDIENTE'
        params.SURTIDOR = 'PENDIENTE'
        return params;
    }

    static String format(def d){
        return """${d.calle}, ${d.noExterior}, ${d.noInterior?:''}, ${d.colonia},${d.codigoPostal}, ${d.municipio}, ${d.localidad},${d.estado}, ${d.pais} """;
    }

    public static  generarQR(Cfdi cfdi) {
        String pattern="?re=${0}&rr={1}&tt={2,number,##########.######}&id,{3}"
        String qq=MessageFormat.format(pattern, cfdi.emisorRfc,cfdi.receptorRfc,cfdi.total,cfdi.uuid)
        File file=QRCode.from(qq).to(ImageType.GIF).withSize(250, 250).file()
        return file.toURI().toURL()

    }

}