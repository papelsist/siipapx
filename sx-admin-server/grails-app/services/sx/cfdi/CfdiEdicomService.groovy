package sx.cfdi

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Log4j
import sx.core.Empresa
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@GrailsCompileStatic
@Transactional
@Log4j
class CfdiEdicomService {

    private Empresa empresa


    SOAPClient client = new SOAPClient("https://cfdiws.sedeb2b.com/EdiwinWS/services/CFDi?wsdl")

    @CompileDynamic
    def getCfdiTest(byte[] data){
        Empresa empresa = getEmpresa()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        SOAPResponse response = client.send(SOAPAction: url, sslTrustAllCerts:true){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                getCfdiTest{
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    file(data.encodeBase64())
                }
            }
        }
        String res = response.getCfdiTestResponse
        return res.decodeBase64()
    }

    @CompileDynamic
    def getCfdi(byte[] data){
        Empresa empresa = getEmpresa()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        SOAPResponse response = client.send(SOAPAction: url, sslTrustAllCerts:true){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                getCfdi{
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    file(data.encodeBase64())
                }
            }
        }
        String res = response.getCfdiResponse
        return res.decodeBase64()
    }

    @CompileDynamic
    getCfdiFromUUID(Cfdi cfdi) {
        Empresa empresa = getEmpresa()
        String url = 'http://cfdi.service.ediwinws.edicom.com'
        SOAPResponse response = client.send(SOAPAction: url, sslTrustAllCerts:true){
            body('xmlns:cfdi': 'http://cfdi.service.ediwinws.edicom.com') {
                getCfdiFromUUID {
                    user(empresa.usuarioPac)
                    password(empresa.passwordPac)
                    rfc(cfdi.emisorRfc)
                    uuid(cfdi.uuid)
                }
            }
        }
        def res = response.getCfdiFromUUIDResponse
        return res.decodeBase64()
    }

    Empresa getEmpresa() {
        if(!empresa) {
            empresa= Empresa.first()
        }
        return empresa
    }
}
