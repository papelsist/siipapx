package com.luxsoft.cfdix.v33

import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.ine.INE
import org.apache.commons.io.FileUtils
import sx.cfdi.ComplementoIne
import sx.core.ClienteCredito
import sx.core.Venta
import sx.core.VentaDet
import sx.utils.MonedaUtils

import java.text.SimpleDateFormat
import java.text.MessageFormat

import sx.cfdi.Cfdi
import com.luxsoft.utils.ImporteALetra

import lx.cfdi.v33.Comprobante
import sx.cfdi.CfdiTimbre

import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType


class V33RemisionGenerator {

    final static SimpleDateFormat CFDI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    static getReportData(Venta venta, envio = true){

        Cfdi cfdi = venta.cuentaPorCobrar.cfdi
        File xmlFile = FileUtils.toFile(cfdi.url)
        Comprobante comprobante = CfdiUtils.read(xmlFile)
        def conceptos = comprobante.conceptos.concepto
        def index = 0
        def modelData=conceptos.collect { cc ->
            def traslado = cc.impuestos.traslados.traslado[0]
            def res=[
                    'cantidad' : cc.getCantidad(),
                    'NoIdentificacion' : cc.noIdentificacion,
                    'descripcion' : cc.descripcion,
                    'unidad': cc.unidad,
                    'ValorUnitario':cc.valorUnitario,
                    'Importe':cc.importe,
                    'ClaveProdServ': cc.claveProdServ,
                    'ClaveUnidad': cc.claveUnidad,
                    'DescuentoImporte': cc.descuento.toString()?: '0.0',
                    'Impuesto': traslado.impuesto.toString(),
                    'TasaOCuota': traslado.tasaOCuota.toString(),
                    'TipoFactor': traslado.tipoFactor.value().toString(),
                    'Base': traslado.base,
                    'TrasladoImporte': traslado.importe

            ]
            if(cc.cuentaPredial){
                res.cuenta_predial = cc.cuentaPredial.numero
            }
            if(cc.informacionAduanera.size() > 0 ){
                def data = cc.informacionAduanera.collect {it.numeroPedimento}.join(',')
                res.pedimento = data
            }
            if (venta && venta.partidas.size()>1) {
                VentaDet partida = venta.partidas.get(index++)
                if (partida) {
                    if (partida.corte) {
                        String ic = "${partida.corte.tipo ?: ''} ${partida.corte.instruccion ?: ''}"
                        res['InstruccionDeCorte'] = ic
                    }
                    res['COMENTARIO'] = partida.comentario
                    res['Descuento'] = partida.descuento.toString()

                }

            }
            return res
        }
        def params = getParametros(cfdi, comprobante, xmlFile, envio)
        def data = [:]
        data['CONCEPTOS'] = modelData
        data['PARAMETROS'] = params
        return data
    }

    static getParametros(Cfdi cfdi, Comprobante comprobante, File xmlFile, boolean envio){
        def params=[:]
        params["VERSION"] = comprobante.version
        params["SERIE"] = comprobante.getSerie()
        params["FOLIO"] = comprobante.getFolio()
        params["NUM_CERTIFICADO"] = comprobante.getNoCertificado()
        params["SELLO_DIGITAL"] = comprobante.getSello()
        params["RECEPTOR_NOMBRE"] = comprobante.getReceptor().getNombre()
        params["RECEPTOR_RFC"] = comprobante.getReceptor().getRfc()
        params["IMPORTE"] = comprobante.getSubTotal() as String
        params["IVA"] = (comprobante?.getImpuestos()?.getTotalImpuestosTrasladados()?: 0.0) as String
        params["TOTAL"] = comprobante.getTotal() as String
        params["RECEPTOR_DIRECCION"] = 'ND'
        params.put("METODO_PAGO", 		comprobante.metodoPago.toString());
        params.put("FORMA_PAGO", 		comprobante.formaPago);
        params.put("IMP_CON_LETRA", 	ImporteALetra.aLetra(comprobante.getTotal()));
        params['FORMA_DE_PAGO']=comprobante.formaPago
        params['PINT_IVA']='16 '
        params["DESCUENTOS"] = comprobante.getDescuento() as String
        BigDecimal descuentos = comprobante.getDescuento()?: 0.0;
        BigDecimal subTotal = comprobante.getSubTotal()?: 0.0
        params["IMPORTE_BRUTO"] = (subTotal - descuentos).toString()
        // params['CONDICIONES_PAGO'] = comprobante.condicionesDePago
        params['UsoCFDI'] = comprobante.receptor.usoCFDI.value().toString()
        params['Moneda'] = comprobante.moneda.value().toString()

        if(comprobante.getReceptor().rfc=='XAXX010101000'){
            params["IMPORTE"] = comprobante.getTotal() as String
        }
        def emisor=comprobante.getEmisor();
        params.put("EMISOR_NOMBRE", 	emisor.getNombre());
        params.put("EMISOR_RFC", 		emisor.getRfc())
        params["EMISOR_DIRECCION"] = ' '
        params["REGIMEN"] = comprobante.emisor.regimenFiscal
        params["LUGAR_EXPEDICION"] = comprobante.lugarExpedicion
        def relacionados = comprobante.cfdiRelacionados
        if(relacionados){
            params.put('RelacionUUID',relacionados.cfdiRelacionado.get(0).UUID)
        }
        if(cfdi.uuid!=null){
            def img = generarQR(cfdi)
            params.put("QR_CODE",img);
            CfdiTimbre timbre = new CfdiTimbre(xmlFile.bytes)
            params.put("FECHA_TIMBRADO", timbre.fechaTimbrado);
            params.put("FOLIO_FISCAL", timbre.uuid);
            params.put("SELLO_DIGITAL_SAT", timbre.selloSAT);
            params.put("CERTIFICADO_SAT", timbre.noCertificadoSAT);
            params.put("CADENA_ORIGINAL_SAT", timbre.cadenaOriginal());
            params.put("RfcProvCertif", timbre.rfcProvCertif)
        }
        params.FECHA = comprobante.fecha
        cargarParametrosAdicionales(cfdi, params, envio)
        if(comprobante.getComplemento()) {
            comprobante.getComplemento().any.each {
                if(it instanceof  INE) {
                    INE ine = (INE)it;
                    StringBuffer buffer = new StringBuffer()
                    buffer.append("INE TipoProceso: ${ine.tipoProceso}")
                    if(ine.tipoComite) {
                        buffer.append(" TipoComite: ${ine.tipoComite}")
                    }
                    if(ine.idContabilidad) {
                        buffer.append(" IdContabilidad: ${ine.idContabilidad}")
                    }
                    if(ine.entidad) {
                        ine.entidad.each{ e ->
                            buffer.append(" ClaveEntidad: ${e.claveEntidad}")
                            if(e.contabilidad) {
                                def contas = e.contabilidad.collect{wt -> wt.idContabilidad}.join(',')
                                buffer.append(" idContabilidad: ${contas}")
                            }
                        }
                    }
                    params.COMPLEMENTO_INE = buffer.toString()
                }
            }
        }
        return params;
    }

    public static  generarQR(Cfdi cfdi) {
        String pattern="?re=${0}&rr={1}&tt={2,number,##########.######}&id,{3}"
        String qq=MessageFormat.format(pattern, cfdi.emisorRfc,cfdi.receptorRfc,cfdi.total,cfdi.uuid)
        File file=QRCode.from(qq).to(ImageType.GIF).withSize(250, 250).file()
        return file.toURI().toURL()

    }

    public static cargarParametrosAdicionales(Cfdi cfdi, Map parametros, boolean envio){
        switch (cfdi.origen) {
            case 'VENTA':
                parametrosAdicionalesVenta(cfdi, parametros, envio)
                break
        }
    }

    public static parametrosAdicionalesVenta(Cfdi cfdi, Map parametros, boolean envio ) {
        Venta venta = Venta.where {cuentaPorCobrar.cfdi == cfdi}.find()

        assert venta, 'No existe la venta origen del CFDI: ' + cfdi.id
        parametros.CLAVCTE = venta.cliente.clave
        parametros.KILOS = venta.kilos.toDouble()
        String tipo = 'CONTADO'
        switch (venta.tipo){
            case 'CRE':
                tipo = 'CREDITO'
                break
            case 'COD':
                tipo = 'COD'
                break
        }
        parametros.TIPO = tipo
        parametros.PEDIDO = venta.documento
        parametros.COMENTARIO = venta.comentario
        parametros.PUESTO = venta.puesto ? 'PUESTO' : null
        parametros.ELAB_FAC = venta.cuentaPorCobrar.updateUser ?: 'ND'
        parametros.ELAB_VTA = venta.createUser ?: 'ND'
        parametros.IMPRESO = venta.impreso
        if(envio){
            parametros.IMPRESO = null
        }

        parametros.FPAGO = venta.cuentaPorCobrar.formaDePago
        parametros.ENVIO = "LOCAL"

        if (venta.envio) {
            parametros.DIR_ENTREGA = venta.envio.direccion.toLabel()
            parametros.ENVIO = venta.envio.condiciones
        }
        if(venta.impreso == null) {
            venta.impreso = new Date()
            venta = venta.save flush:true
        }
        if (venta.tipo == 'CRE' && venta.cliente.credito) {
            ClienteCredito credito = venta.cliente.credito
            String cdp = "PLZ: ${credito.plazo} DIAS ${credito.venceFactura ? 'FAC' : 'REV'}D. REV:${credito.diaRevision} D.COB:${credito.diaCobro} VEND: ${venta.cliente?.vendedor?.sw2} COB: ${credito?.cobrador?.sw2}"
            parametros['CONDICIONES_PAGO'] = cdp

        }

        parametros.TELEFONOS = venta.cliente.getTelefonos().join('/')

        if(venta.moneda != MonedaUtils.PESOS) {
            parametros.put("IMP_CON_LETRA", 	ImporteALetra.aLetraDolares(venta.getTotal()));
        }
        if(venta.socio) {
            parametros.SOCIO = venta.socio.nombre
        }

    }

    static String format(def d){
        return """${d.calle}, ${d.noExterior}, ${d.noInterior?:''}, ${d.colonia},${d.codigoPostal}, ${d.municipio}, ${d.localidad},${d.estado}, ${d.pais} """;
    }

}
