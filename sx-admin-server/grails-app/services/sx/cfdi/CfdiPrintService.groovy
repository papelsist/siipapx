package sx.cfdi

import com.luxsoft.cfdix.v33.V33PdfGenerator
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import grails.web.context.ServletContextHolder
import sx.reports.ReportService

/**
 * Servicios de impresion de facturas centralizados
 *
 */
class CfdiPrintService {

    CfdiLocationService cfdiLocationService

    ReportService reportService

    def getPdf(Cfdi cfdi){
        String fileName = cfdi.url.getPath().substring(cfdi.url.getPath().lastIndexOf('/')+1)
        fileName = fileName.replaceAll('.xml', '.pdf')
        File file = new File(cfdiLocationService.getCfdiLocation(cfdi), fileName)
        if(file.exists()){
            return file
        } else {
            log.info('No hay impresion generada, generando archivo PDF');
            ByteArrayOutputStream out = generarPdfFactura(cfdi)
            file.setBytes(out.toByteArray())
            return file
        }
    }

    def generarPdfFactura(Cfdi cfdi){
        def realPath = ServletContextHolder.getServletContext().getRealPath("reports") ?: 'reports'
        // def data = V33PdfGenerator.getReportData(cfdi, cfdiLocationService.getXml(cfdi), true)
        def data = V33PdfGenerator.getReportData(cfdi, true)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }
}
