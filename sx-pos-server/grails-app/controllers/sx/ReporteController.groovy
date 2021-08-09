package sx

import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ReporteController {

	// static responseFormats = ['json', 'xml']

    def jasperService

    def run(ReportConfig config) {
        // println "Generando  Reporte... con params: " + params
        def repParams = params
        println "Reporte: " + params
        def reportDef= new JasperReportDef(
                name: repParams.name,
                fileFormat: JasperExportFormat.PDF_FORMAT,
                parameters: repParams
        )
        ByteArrayOutputStream pdfStream = jasperService.generateReport(reportDef)
        render (file: pdfStream.toByteArray(), contentType: 'application/pdf', filename: 'MovimientoGenerico')
        
    }
}

class  ReportConfig {
    
    String name
    Map params = [:]
    Map data = [:]
    String fileName

    static constraints = {
    }

    String toString(){
        return "Reporte ${name}"
    }
}
