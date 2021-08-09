package sx.cfdi

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.NodeChildren
import groovy.xml.XmlUtil
import org.bouncycastle.util.encoders.Base64
import sx.core.Empresa
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@CompileStatic
class CancelacionService {

    SOAPClient client = new SOAPClient("https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl")

    @CompileDynamic
    def cancelar(Cfdi cfdi){
        CfdiCancelado found = CfdiCancelado.where{uuid == cfdi.uuid}.find()
        assert found == null, "UUID: ${cfdi.uuid} ya cancelado"
        Empresa empresa = Empresa.first()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        SOAPResponse response = client.send(SOAPAction: url, sslTrustAllCerts:true){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                cancelaCFDi {
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    rfc(cfdi.emisorRfc)
                    uuid(cfdi.uuid)
                    pfx(empresa.getCertificadoDigitalPfx().encodeBase64())
                    PfxPassword('pfxfilepapel')
                }
            }
        }
        def res = response.cancelaCFDiResponse
        def xml = new XmlSlurper().parseText(XmlUtil.serialize(res))
        String xmlString = new String(xml.cancelaCFDiReturn.ack.decodeBase64())
        xmlString = XmlUtil.serialize(xmlString)
        def xml2 = new XmlSlurper().parseText(xmlString)
        def folios = xml2.breadthFirst().find { it.name() == 'Folios'}
        if (folios ) {
            // println 'UUID: ' +folios.UUID
            // println 'Status: ' + folios.EstatusUUID
            log.debug('Cancelacion de cfdi: {} status: {}' , folios.UUID, folios.EstatusUUID)
            CfdiCancelado cancelacion = new CfdiCancelado()
            cancelacion.cfdi = cfdi
            cancelacion.uuid = cfdi.uuid
            cancelacion.serie = cfdi.serie
            cancelacion.folio = cfdi.folio
            cancelacion.aka = xmlString.bytes
            cancelacion.statusSat = folios.EstatusUUID
            cancelacion.save failOnError: true, flush: true
            cfdi.cancelado = true
            cfdi.status = 'CANCELADO EN EL SAT'
            cfdi.save flush: true
            cancelacion.save failOnError: true, flush: true
            return cancelacion
        }
    }


}
