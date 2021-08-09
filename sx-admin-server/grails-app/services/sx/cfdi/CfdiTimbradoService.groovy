package sx.cfdi

import grails.gorm.transactions.Transactional
import grails.util.Environment
import org.apache.commons.io.FileUtils

import com.luxsoft.utils.ZipUtils
import org.bouncycastle.util.encoders.Base64
import sx.cfdi.providers.edicom.CFDi
import sx.cfdi.providers.edicom.CancelaResponse
import sx.core.Empresa

/*
import sx.cfdi.providers.cepdi.DatosExtra
import sx.cfdi.providers.cepdi.RespuestaTimbrado
import sx.cfdi.providers.cepdi.WS
*/

/**
 * Prueba de service para timbrado
 *
 */
@Transactional
class CfdiTimbradoService {

    // WS cerpiService
    CFDi edicomService
    CfdiEdicomService cfdiEdicomService

    Empresa empresaTransient

    def timbrar(Cfdi cfdi) {
        timbrarEdicom(cfdi)
        return cfdi
    }

    /**
     * Web Service SAOP (wsdl) para los diversos servicios de timbado proporcionados por EDICOM. Actualmente
     * se requiere habilitar un certificado inadecuado
     * Utilizando (en Mac o unix): sudo keytool -importcert -alias edicom -file ~/dumps/certificados/caedicom01.cer -keystore cacerts;
     *
     */
    def timbrarEdicom(Cfdi cfdi) {
        File file = FileUtils.toFile(cfdi.url)
        // log.debug('Timbrando archivo {}' ,file.getPath())
        byte[] res = null;
        if (this.isTimbradoDePrueba()) {
            log.debug('Timbrado de prueba')
            // res = edicomService.getCfdiTest('PAP830101CR3','yqjvqfofb', file.bytes)
            res = cfdiEdicomService.getCfdiTest(file.bytes)
        } else {
            res = cfdiEdicomService.getCfdi(file.bytes)
        }

        Map map = ZipUtils.descomprimir(res)

        def entry = map.entrySet().iterator().next()
        def targetName = file.getName().replaceAll(".xml", "_SIGNED.xml")
        File target = new File(file.getParent(), targetName)

        FileUtils.writeByteArrayToFile(target, entry.getValue())
        log.debug('Timbrado OK Archio: {}', target.getPath())
        CfdiTimbre timbre = new CfdiTimbre(entry.getValue())
        cfdi.uuid = timbre.uuid
        cfdi.url = target.toURI().toURL()
        cfdi.fileName = targetName
        cfdi.save flush: true
    }

    def getCfdiFromUUID(Cfdi cfdi) {
        byte[] res = cfdiEdicomService.getCfdiFromUUID(cfdi);
        Map map = ZipUtils.descomprimir(res)
        def dir = cfdiService.getCfdiLocation(cfdi)
        File f = new File('')
        f.toURI().toURL()

    }

    /*
    def timbrarCerpi() {
      File file = FileUtils.toFile(cfdi.url)
      DatosExtra extra = new DatosExtra()
      RespuestaTimbrado res = cerpiService.timbraXML('clientetimbrado001@mail.com','Demo123#',file.toString(), extra)
      if(res.exitoso) {
        cfdi.uuid = res.UUID
        println 'TFD: ' + res.TFD
        return res.XMLTimbrado
      } else {
        println( 'Error timbrando XML: ' + res.mensajeError + ' Código: ' + res.codigoError)
        return null

      }
    }
    */

    /**
     * Cancela el CFDI utilizando el servicio del proveedor de timbrado activo
     *
     * @param cfdi
     * @return
     */
    def cancelar(Cfdi cfdi){
        cancelarEdicom(cfdi);

    }

    /**
     * Cancela el CFDI utilizando el serivio de EDICOM
     *
     * @param cfdi
     * @return
     */
    def cancelarEdicom(Cfdi cfdi) {
        assert cfdi.uuid, "El Cfdi ${cfdi.serie} - ${cfdi.folio} no se a timbrado por lo que no puede ser cancelado"
        CfdiCancelado cancelacion = new CfdiCancelado()
        cancelacion.cfdi = cfdi
        cancelacion.uuid = cfdi.uuid
        cancelacion.serie = cfdi.serie
        cancelacion.folio = cfdi.folio

        // Iniciando cancelacion
        Empresa empresa = getEmpresa()
        String[] uuids = [cfdi.uuid] as String[]
        log.debug('Cancelando: {}' , cfdi.uuid)
        edicomService.cancelaCFDi(
                empresa.usuarioPac,
                empresa.passwordPac,
                empresa.rfc,
                uuids,
                empresa.certificadoDigitalPfx,
                'pfxfilepapel')
        /*

        CancelaResponse response = edicomService.cancelaCFDi(
                empresa.usuarioPac,
                empresa.passwordPac,
                empresa.rfc,
                [cfdi.uuid],
                empresa.certificadoDigitalPfx,
                'pfxfilepapel')

        response.getUuids().getItem().each {
            log.debug('UUID Cancelado: ', it)
        }

        String msg=response.getText()
        log.debug('Cancelacion text: ',mes)
        log.debug('Cancelacion Base64.decore: ', Base64.decode(msg.getBytes()))
        cancelacion.message = Base64.decode(msg.getBytes())

        String aka=response.getAck()
        cancelacion.aka=Base64.decode(aka.getBytes())
        cancelacion.save failOnError: true, flush: true

        cfdi.cancelado = true
        cfdi.status = 'CANCELADO'
        cfdi.save flush: true
        cancelacion.save failOnError: true, flush: true
        log.debug(" CFDI: ${cfdi.serie} - ${cfdi.folio} cancelado exitosamente")
        return cancelacion
        */
    }

    Boolean isTimbradoDePrueba() {
        Boolean produccion = (Environment.current == Environment.PRODUCTION)
        Boolean queretaro = Environment.current.name == 'queretaro'
        return !(produccion || queretaro)
    }

    Empresa getEmpresa() {
        if(!empresaTransient) {
            empresaTransient = Empresa.first()
        }
        return empresaTransient
    }

}
