package com.luxsoft.cfdix.v33

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.apache.commons.io.FileUtils

import javax.xml.bind.JAXBContext



import sx.cfdi.Cfdi
import lx.cfdi.v33.Comprobante
import lx.cfdi.v33.CMoneda

import lx.cfdi.v33.CfdiUtils
import sx.utils.MonedaUtils


class V33CfdiUtils {

	static Comprobante toComprobante(Cfdi cfdi){
    File file = FileUtils.toFile(cfdi.url)
    Comprobante comprobante = CfdiUtils.read(file.bytes)
    return comprobante
	}

    static List getPartidas(Cfdi cfdi) {
        Comprobante comprobante = CfdiUtils.read(cfdi.url.bytes)
        comprobante.getConceptos().concepto
    }

  static String parse(byte[] xmlData){
    ByteArrayInputStream is=new ByteArrayInputStream(xmlData)
    GPathResult xmlResult = new XmlSlurper().parse(is)
    return XmlUtil.serialize(xmlResult)
  }

	static getMonedaCode(Currency moneda){
		if(moneda == MonedaUtils.PESOS) 
			return CMoneda.MXN
		else 
			return CMoneda.USD
	}

}