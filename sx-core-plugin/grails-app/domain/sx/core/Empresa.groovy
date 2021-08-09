package sx.core

import org.apache.commons.lang3.exception.ExceptionUtils

import java.security.*
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

import java.security.spec.PKCS8EncodedKeySpec


class Empresa {

    String id

    String clave

    String nombre

    Direccion direccion

    String rfc

    String regimen

    String numeroDeCertificado

    byte[] certificadoDigital

    byte[] certificadoDigitalPfx

    byte[] llavePrivada

    String passwordPfx

    X509Certificate certificado

    PrivateKey privateKey

    boolean timbradoDePrueba=true

    String regimenClaveSat

    String usuarioPac

    String passwordPac

    Date dateCreated

    Date lastUpdated



    static embedded = ['direccion']


    static  mapping={
        id generator:'uuid'
    }


    static constraints = {
      clave size:3..15,unique:true
      nombre(blank:false,maxSize:255,unique:true)
      rfc(blank:false,minSize:12,maxSize:13)
      direccion(nullable:false)
      regimen (blank:false,maxSize:300)
      numeroDeCertificado(nullable:true,minSize:1,maxSize:20)
      certificadoDigital(nullable:true,maxSize:1024*1024*5)
      certificadoDigitalPfx(nullable:true,maxSize:1024*1024*2)
      llavePrivada(nullable:true,maxSize:1024*1024*5)
      passwordPfx nullable:true
      usuarioPac nullable:true
      passwordPac nullable:true
      regimenClaveSat nullable: true

    }

    static transients = ['certificado','certificadoPfx','privateKey']



    X509Certificate getCertificado(){

        if(certificadoDigital && !certificado){
            //assert certificadoDigital,'Debe cargar el binario del certificado '
            try {

                // log.info('Cargando certificado digital en formato X509')
                CertificateFactory fact= CertificateFactory.getInstance("X.509","BC")
                InputStream is=new ByteArrayInputStream(certificadoDigital)
                certificado = (X509Certificate)fact.generateCertificate(is)
                certificado.checkValidity()
                //is.closeQuietly();
                is.close();
                this.certificado=certificado
            } catch (Exception e) {
                e.printStackTrace()
                println 'Error tratando de leer certificado en formato X509 :'+ExceptionUtils.getRootCauseMessage(e)
            }


        }

        return certificado;
    }

    String getCertificadoInfo(){
        return "$certificado?.subjectX500Principal"
    }

    PrivateKey getPrivateKey(){
        if(!privateKey && llavePrivada){
            try {
                final byte[] encodedKey=llavePrivada
                PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(encodedKey)
                final  KeyFactory keyFactory=KeyFactory.getInstance("RSA","BC")
                this.privateKey=keyFactory.generatePrivate(keySpec)
            } catch (Exception e) {
                e.printStackTrace()
                // log.error 'Error tratando de leer llave privada :'+ExceptionUtils.getRootCauseMessage(e)
            }

        }
        return privateKey;
    }

    def getCertificadoPfx(){

        if(certificadoDigitalPfx){
            return certificadoDigitalPfx.encodeBase64().toString()
        }
        return "ND";
    }

    String toString(){
        return "$nombre ($rfc)"
    }
}


