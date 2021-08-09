package sx.cfdi

import com.luxsoft.cfdix.CFDIXUtils
import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.gorm.transactions.Transactional
import grails.web.context.ServletContextHolder
import groovy.xml.XmlUtil
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.ine.IneUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.grails.core.io.ResourceLocator
import sx.core.AppConfig
import sx.core.Venta
import sx.reports.ReportService

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import grails.util.Environment

@Transactional
class CfdiService {

    private AppConfig config

    ReportService reportService

    ResourceLocator resourceLocator

    def grailsResourceLocator

    Cfdi generarCfdi(Comprobante comprobante, String tipo, boolean ine = false) {
        Cfdi cfdi = new Cfdi()
        cfdi.tipoDeComprobante = tipo
        cfdi.fecha = Date.parse( "yyyy-MM-dd'T'HH:mm:ss", comprobante.fecha,)
        cfdi.serie = comprobante.serie
        cfdi.folio = comprobante.folio
        cfdi.emisor = comprobante.emisor.nombre
        cfdi.emisorRfc = comprobante.emisor.getRfc()
        cfdi.receptor = comprobante.receptor.nombre
        cfdi.receptorRfc = comprobante.receptor.rfc
        cfdi.total = comprobante.total
        cfdi.fileName = getFileName(cfdi)
        // cfdi.url = "${getDirPath(cfdi)}/${getFileName(cfdi)}"
        try {
            if(ine){
                saveXml(cfdi, IneUtils.toXmlByteArray(comprobante))
            }else {
                saveXml(cfdi, CfdiUtils.toXmlByteArray(comprobante))
            }
            cfdi.save failOnError: true, flush:true
            return cfdi
        }catch (Exception ex) {
            //ex.printStackTrace()
            throw ex
        }

    }

    void saveXml(Cfdi cfdi, byte[] data){
        def date = cfdi.fecha
        String year = date[Calendar.YEAR]
        String month = date[Calendar.MONTH]+1
        String day = date[Calendar.DATE]
        def cfdiRootDir = new File(getConfig().cfdiLocation?: "${System.properties['user.home']}/cfdis")
        final FileTreeBuilder treeBuilder = new FileTreeBuilder(cfdiRootDir)
        treeBuilder{
            dir(cfdi.emisor){
                dir(year){
                    dir(month){
                        dir(day){
                            File res = file(cfdi.fileName) {
                                setBytes(data)
                            }
                            cfdi.url = res.toURI().toURL()
                        }
                    }
                }
            }
        }
    }

    def save(Comprobante comprobante, String dirPath, String fileName){

        File dir = new File(dirPath)
        if(!dir.exists()) {
            dir.mkdirs()
        }
        byte[] data = CfdiUtils.toXmlByteArray(comprobante)
        File xmlFile = new File(dir, fileName);
        FileOutputStream os = new FileOutputStream(xmlFile, false)
        os.write(data)
        os.flush()
        os.close()
    }


    private getDirPath(Cfdi cfdi) {
        String dirPath = "${getConfig().cfdiLocation}/${cfdi.emisor}/${cfdi.fecha.format('YYYY')}/${cfdi.fecha.format('MM')}"
        return dirPath
    }

    private getFileName(Cfdi cfdi){
        String name = "${cfdi.receptorRfc}-${cfdi.serie}-${cfdi.folio}.xml"
        return name
    }


    AppConfig getConfig() {
        return AppConfig.first()
    }

    def cancelar(Cfdi cfdi) {
        if (cfdi.uuid) {
            // Todo Cancelar en el SAT
        } else {
            cfdi.delete flush:true
        }
    }

    def enviar(Cfdi cfdi, String targetEmail) {
        assert !cfdi.cancelado, "CFDI ${cfdi.serie} ${cfdi.folio}  cancelado no se puede enviar por correo"
        assert cfdi.uuid, "El CFDI ${cfdi.serie} ${cfdi.folio} no se ha timbrado"
        if (cfdi.origen == 'VENTA') {
            Venta venta = Venta.where {cuentaPorCobrar.cfdi == cfdi}.find()
            assert venta, "No existe la venta origen del cfdi: ${cfdi.id}"
            String email = targetEmail ?: venta.cliente.getCfdiMail()
            if (!email) {
                cfdi.comentario = "CLIENTE ${venta.cliente.getCfdiMail()} SIN CORREO PARA ENVIO DE EMAIL "
                cfdi.save flush: true
                return cfdi
            }
            return enviarFacturaEmail(cfdi, venta, email)
        }
    }

    def enviarFacturaEmail(Cfdi cfdi, Venta factura, String targetEmail) {
        log.debug('Enviando cfdi {} {} al correo: {}', cfdi.serie,cfdi.folio, targetEmail)
        println "Enviando cfdi ${cfdi.serie} - ${cfdi.folio} al correo: ${targetEmail}"

        def xml = cfdi.getUrl().getBytes()
        def pdf = generarImpresionV33(cfdi).toByteArray()

        String message = """Apreciable cliente por este medio le hacemos llegar la factura electrónica de su compra. Este correo se envía de manera autmática favor de no responder a la dirección del mismo. Cualquier duda o aclaración 
            la puede dirigir a: servicioaclientes@papelsa.com.mx 
        """
        try {
            sendMail {
                multipart false
                to targetEmail
                from 'facturacion@papelsa.mobi'
                subject "Envio de CFDI ${cfdi.serie} ${cfdi.folio}"
                text message
                attach("${cfdi.serie}-${cfdi.folio}.xml", 'text/xml', xml)
                attach("${cfdi.serie}-${cfdi.folio}.pdf", 'application/pdf', pdf)
            }
            cfdi.enviado = new Date()
            cfdi.email = targetEmail
            cfdi.save()
        } catch (Exception ex) {
            String c = ExceptionUtils.getRootCauseMessage(ex)
            log.debug('Error enviando correo Fac: {} Error: {}', venta.statusInfo(), c)
            cfdi.enviado = new Date()
            cfdi.email = targetEmail
            cfdi.comentario = "Error en evio: ${c}"
            cfdi.save flush:true
        }

    }


    public generarImpresionV33( Cfdi cfdi) {
        def logoPath = ServletContextHolder.getServletContext().getRealPath("reports/PAPEL_CFDI_LOGO.jpg")
        def data = V33PdfGenerator.getReportData(cfdi, true)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = logoPath

        println "Imprimiendo el reporte de la factura de todos"

        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
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
                zos.write(cfdi.getUrl().getBytes())
                zos.closeEntry();
                // PDF
                def pdf =   generarImpresionV33(cfdi)
                ZipEntry pdfEntry = new ZipEntry(name.replaceAll('xml', 'pdf'))
                zos.putNextEntry(pdfEntry);
                zos.write(pdf.toByteArray())
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


    def envioBatch(List cfdis, String targetEmail, String observacion = ''){
        def zipData = zip(cfdis)

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

    Boolean isTimbradoDePrueba() {
        Boolean produccion = (Environment.current == Environment.PRODUCTION)
        Boolean queretaro = Environment.current.name == 'queretaro'
        return !(produccion || queretaro)
    }



}
