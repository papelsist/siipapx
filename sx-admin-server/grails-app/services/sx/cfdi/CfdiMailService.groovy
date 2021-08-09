package sx.cfdi

import org.apache.commons.lang3.exception.ExceptionUtils

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class CfdiMailService {

    CfdiPrintService cfdiPrintService
    CfdiLocationService cfdiLocationService

    def envioBatch(List cfdis, String targetEmail, String observacion = ''){
        def zipData = zip(cfdis)
        println 'Zip generado: ' + zipData.length

        sendMail {
            multipart false
            to targetEmail
            from 'facturacion@papelsa.mobi'
            subject "Envio de Comprobantes fiscales digitales (CFDIs) ${observacion}"
            html view: "/cfdi/envioBatch", model: [facturas: cfdis]
            attachBytes "comprobantes.zip", "application/x-compressed", zipData
        }
        cfdis.each { Cfdi cfdi ->
            try {
                cfdi.enviado = new Date()
                cfdi.email = targetEmail
                cfdi.save()
            } catch (Exception ex){

            }

        }

    }

    Byte[] zip(List cfdis){
        try{
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)

            cfdis.each { Cfdi cfdi ->
                String name = "${cfdi.serie}-${cfdi.folio}.xml"
                ZipEntry ze = new ZipEntry(name);
                zos.putNextEntry(ze);
                Byte[] xml = cfdiLocationService.getXml(cfdi)
                zos.write(xml)
                zos.closeEntry();
                // PDF
                def pdf = cfdiPrintService.getPdf(cfdi)
                ZipEntry pdfEntry = new ZipEntry(name.replaceAll('xml', 'pdf'))
                zos.putNextEntry(pdfEntry);
                zos.write(pdf.bytes)
                zos.closeEntry();
            }
            zos.close();
            return byteArrayOutputStream.toByteArray()
        }catch (IOException ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)
            println msg
            log.error(msg)
        }
    }
}
