package sx.cloud

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import groovy.xml.XmlUtil
import groovy.util.logging.Slf4j

import grails.util.Environment
import grails.gorm.transactions.Transactional
import grails.web.context.ServletContextHolder
import org.grails.core.io.ResourceLocator


import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.io.FileUtils

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo

import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante

import com.luxsoft.cfdix.CFDIXUtils
import com.luxsoft.cfdix.v33.V33PdfGeneratorPos

import sx.cfdi.Cfdi

import sx.reports.ReportService
import sx.cloud.FirebaseService


@Slf4j
class LxCfdiService {

    ReportService reportService

    ResourceLocator grailsResourceLocator

    FirebaseService firebaseService

    ByteArrayOutputStream generarPdf( Cfdi cfdi, boolean envio = true, boolean actualizar = false) {
        def realPath = grailsResourceLocator.findResourceForURI("/reports").getURI().getPath() ?: 'reports'
        File xmlFile = FileUtils.toFile(cfdi.url)
        def data = V33PdfGeneratorPos.getReportData(cfdi, envio)
        Map parametros = data['PARAMETROS']
        parametros.PAPELSA = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        return reportService.run('PapelCFDI3.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
    }

    def pushToFireStorage(Cfdi cfdi) {
        pushPdf(cfdi)
        pushXml(cfdi)
    }

    def pushPdf(Cfdi cfdi) {

        String objectName =buildOjbectName(cfdi, 'pdf')
        def rawData = this.generarPdf(cfdi)
        def data = rawData.toByteArray()
        
        publishCfdiDocument(objectName, data, "application/pdf", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'pdf'])
        log.info('Factura {} publicada en firebase exitosamente', objectName)

    }

    def pushXml(Cfdi cfdi) {
        // Object
        String objectName =buildOjbectName(cfdi, 'xml')
        def data = cfdi.getUrl().getBytes()
        publishCfdiDocument(objectName, data, "text/xml", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'xml'])
        log.info('Factura {} publicada en firebase exitosamente', objectName)

    }

    def publishCfdiDocument(String objectName, def data, String contentType, Map metaData) {
        String projectId = firebaseService.projectId //'siipapx-436ce'
        String bucketName = firebaseService.firebaseBucket // 'siipapx-436ce.appspot.com'
        Storage storage = StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .getService()

        BlobId blobId = BlobId.of(bucketName, objectName)
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .setMetadata(metaData)
            .build()

        storage.create(blobInfo,data)
        log.info('Documento {} publicada EXITOSAMENTE en firebase', objectName)
    }

    String buildOjbectName(Cfdi cfdi, String sufix) {
        return "cfdis/${cfdi.serie}-${cfdi.folio}.${sufix}"
    }


    



}
