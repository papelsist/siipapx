package sx.cfdi

import org.apache.commons.lang3.exception.ExceptionUtils

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.luxsoft.cfdix.v33.V33PdfGenerator
import groovy.util.logging.Slf4j
import org.grails.core.io.ResourceLocator
import sx.cloud.FirebaseService
import sx.cloud.papws.CloudService
import sx.core.AppConfig
import sx.reports.ReportService

@Slf4j
class CfdiPdfService {

    private AppConfig config

    ReportService reportService

    ResourceLocator grailsResourceLocator

    FirebaseService firebaseService

    CloudService cloudService

    ByteArrayOutputStream generarPdf( Cfdi cfdi, boolean envio = true, boolean actualizar = false) {
        def realPath = grailsResourceLocator.findResourceForURI("/reports").getURI().getPath() ?: 'reports'
        def data = V33PdfGenerator.getReportData(cfdi, envio, actualizar)
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
        byte[] data = rawData.toByteArray()
        try {
            log.debug('Subiendo a Firestore en Callcenter 1')
            publishCfdiDocument(objectName, data, "application/pdf", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'pdf'])
            log.info('Factura {} publicada en firebase CALLCENTER 1 exitosamente', objectName)
        }
        catch(Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error subiendo factura  a firestore CALLCENTER 1: ' + message)
        }
        this.cloudService.publishDocument(objectName, data, "application/pdf", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'pdf'])
    }

    def pushXml(Cfdi cfdi) {
        // Object
        String objectName =buildOjbectName(cfdi, 'xml')
        def data = cfdi.getUrl().getBytes()
        try {
            publishCfdiDocument(objectName, data, "text/xml", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'xml'])
            log.info('Factura {} publicada en firebase CALLCENTER 1exitosamente', objectName)
        } catch(Exception ex) {
            String message = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error subiendo factura  a firestore CALLCENTER 1: ' + message)
        }
        this.cloudService.publishDocument(objectName, data, "text/xml", [size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc, tipoArchivo: 'xml'])
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


    /**
    *
    * @Deprecated: Usuar pushPdfDocument
    **/
    def pushPdf_Old(Cfdi cfdi) {
        String objectName = "cfdis/${cfdi.serie}-${cfdi.folio}.pdf"
        def rawData = this.generarPdf(cfdi)
        def data = rawData.toByteArray()
        
        String projectId = firebaseService.projectId //'siipapx-436ce'
        String bucketName = firebaseService.firebaseBucket // 'siipapx-436ce.appspot.com'
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService()

        BlobId blobId = BlobId.of(bucketName, objectName)
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("application/pdf")
            .setMetadata([size: data.length, uuid: cfdi.uuid, receptorRfc: cfdi.receptorRfc])
            .build()

        storage.create(blobInfo,data)
        log.info('Factura {} publicada en firebase exitosamente', objectName)

    }



}
