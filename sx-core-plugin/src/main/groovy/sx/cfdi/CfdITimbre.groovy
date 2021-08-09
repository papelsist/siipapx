package sx.cfdi

import groovy.transform.ToString

import java.text.MessageFormat

@ToString(includeNames=true,includePackage=false)
class CfdiTimbre {
	
	String version
	String uuid
	String fechaTimbrado
	String selloCFD
	String selloSAT
	String noCertificadoSAT
	String rfcProvCertif
	
	CfdiTimbre(byte[] xml){
		build(xml)
	}

	def build(byte[] data){

		ByteArrayInputStream is=new ByteArrayInputStream(data)
		def xml = new XmlSlurper().parse(is)
		def timbre=xml.breadthFirst().find { it.name() == 'TimbreFiscalDigital'}
		
		this.version = timbre.attributes()['Version']
		this.uuid = timbre.attributes()['UUID']
		this.fechaTimbrado = timbre.attributes()['FechaTimbrado']
		this.selloCFD = timbre.attributes()['SelloCFD']
		this.selloSAT = timbre.attributes()['SelloSAT']
		this.noCertificadoSAT = timbre.attributes()['NoCertificadoSAT']
		this.rfcProvCertif = timbre.attributes()['RfcProvCertif']
	    
	}
	
	public String cadenaOriginal(){
		String pattern="||{0}|{1}|{2}|{3}|{4}||";
		return MessageFormat.format(pattern, version,uuid,fechaTimbrado,selloCFD,noCertificadoSAT)
	}

	Date convertFechaTimbraro(){
		return Date.parse("yyyy-MM-dd'T'HH:mm:ss",fechaTimbrado)
	}

}
