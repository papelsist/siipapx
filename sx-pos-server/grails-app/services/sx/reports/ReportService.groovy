package sx.reports

import grails.compiler.GrailsCompileStatic
import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.plugins.jasper.JasperService

import grails.gorm.transactions.Transactional
import net.sf.jasperreports.export.PdfExporterConfiguration
import sx.inventario.Conteo

@Transactional
@GrailsCompileStatic
class ReportService {

    JasperService jasperService

    ByteArrayOutputStream run(String reportName, Map params ) {
        log.debug("Ejecutando reporte ${reportName} con parametros: ${params}")
        def reportDef= new JasperReportDef(
                name: reportName,
                fileFormat: JasperExportFormat.PDF_FORMAT,
                parameters: params
        )
        ByteArrayOutputStream pdfStream = jasperService.generateReport(reportDef)
        return pdfStream
    }

    ByteArrayOutputStream run(String reportName, Map params, Collection data){
        // log.debug("Ejecutando reporte {} con parametros: ${params} y data: ${data}", reportName)
        log.debug("Ejecutando reporte {}", reportName)
        def reportDef=new JasperReportDef(
                name:reportName,
                fileFormat: JasperExportFormat.PDF_FORMAT,
                parameters:params,
                reportData:data
        )
        ByteArrayOutputStream  stream=jasperService.generateReport(reportDef)
        return stream
    }

    ByteArrayOutputStream imprimirFactura(String reportName, Map params, Collection data){

        // log.debug("Ejecutando reporte {} con parametros: ${params} y data: ${data}", reportName)
        log.debug("Ejecutando reporte {} de factura", reportName)


        def reportes = []

        for(def i=1 ; i<=2 ; i++){


            if(i == 1){
                def reportDef1=new JasperReportDef(
                        name:reportName,
                        fileFormat: JasperExportFormat.PDF_FORMAT,
                        parameters:params,
                        reportData:data
                )

                reportes.add(reportDef1)

            }

            if(i == 2){
                def reportDef2=new JasperReportDef(
                        name:reportName+'copia',
                        fileFormat: JasperExportFormat.PDF_FORMAT,
                        parameters:params,
                        reportData:data
                )

                reportes.add(reportDef2)
            }
        }
        ByteArrayOutputStream  stream=jasperService.generateReport(reportes)
        return stream
    }

    ByteArrayOutputStream imprimirSectoresConteo(String reportName, List<Conteo> sectores){

        // log.debug("Ejecutando reporte {} con parametros: ${params} y data: ${data}", reportName)
        log.debug("Ejecutando reporte {}", reportName)


        def reportes = []

        sectores.each{sector -> 
            Map parametros = [:]
            parametros.SECTOR = sector.id
            def reportDef=new JasperReportDef(
                        name:reportName,
                        fileFormat: JasperExportFormat.PDF_FORMAT,
                        parameters:parametros
                        
                )

                reportes.add(reportDef)

        }
        
        ByteArrayOutputStream  stream=jasperService.generateReport(reportes)
        return stream
    }


    ByteArrayOutputStream imprimirRemision(String reportName, Map params, Collection data){

        // log.debug("Ejecutando reporte {} con parametros: ${params} y data: ${data}", reportName)
        log.debug("Ejecutando reporte {}", reportName)


        def reportes = []

        for(def i=1 ; i<=2 ; i++){


            if(i == 1){
                def reportDef1=new JasperReportDef(
                        name:reportName,
                        fileFormat: JasperExportFormat.PDF_FORMAT,
                        parameters:params,
                        reportData:data
                )

                reportes.add(reportDef1)

            }

            if(i == 2){
                def reportDef2=new JasperReportDef(
                        name:'PapelRemisionCFDI3Copia.jrxml',
                        fileFormat: JasperExportFormat.PDF_FORMAT,
                        parameters:params,
                        reportData:data
                )

                reportes.add(reportDef2)
            }
        }
        ByteArrayOutputStream  stream=jasperService.generateReport(reportes)
        return stream
    }

}
