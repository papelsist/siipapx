package com.luxsoft.cfdix


import groovy.xml.*
import groovy.util.slurpersupport.GPathResult
import sx.cfdi.CfdiTimbre

import java.text.SimpleDateFormat

import sx.cfdi.Cfdi


class CFDIXUtils {

	final static SimpleDateFormat CFDI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

	static Cfdi cargarComprobante(Cfdi cfdi, byte[] data){

		ByteArrayInputStream is=new ByteArrayInputStream(data)
		def xml = new XmlSlurper().parse(is)
    	def timbre = xml.breadthFirst().find { it.name() == 'TimbreFiscalDigital'}
	    
		cfdi.uuid = timbre.attributes()['UUID']
		cfdi.serie = xml.attributes()['serie']
		cfdi.folio = xml.attributes()['folio']
		cfdi.fecha = CFDI_DATE_FORMAT.parse(xml.attributes()['fecha'])
	    cfdi.total = xml.attributes()['total'] as BigDecimal
	    cfdi.tipo = xml.attributes()['tipoDeComprobante'].toUpperCase()
	    def emisor = xml.breadthFirst().find { it.name() == 'Receptor'}
		cfdi.emisor = emisor.attributes()['nombre']
		cfdi.rfc = emisor.attributes()['rfc']
	    
	    def receptor = xml.breadthFirst().find { it.name() == 'Receptor'}
		cfdi.receptor = receptor.attributes()['nombre']
		cfdi.receptorRfc = receptor.attributes()['rfc']
		return cfdi
	}

	static String parse(byte[] xmlData){
		ByteArrayInputStream is=new ByteArrayInputStream(xmlData)
		GPathResult xmlResult = new XmlSlurper().parse(is)
		return XmlUtil.serialize(xmlResult)
	}

	static CfdiTimbre getTimbre(Cfdi cfdi){
		def timbre = new CfdiTimbre(cfdi)
		return timbre
	}

}