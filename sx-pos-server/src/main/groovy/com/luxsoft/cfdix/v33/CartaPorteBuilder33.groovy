package com.luxsoft.cfdix.v33

import groovy.util.logging.Slf4j
import lx.cfdi.utils.DateUtils
import lx.cfdi.v33.*
// import org.apache.commons.logging.LogFactory
import org.bouncycastle.util.encoders.Base64
import sx.core.Empresa
import sx.inventario.Traslado
import sx.inventario.TrasladoDet


// Catalogos
import sx.utils.MonedaUtils

//
/**
 * TODO: Parametrizar el regimenFiscal de
 */
@Slf4j
class CartaPorteBuilder {


    // private static final log=LogFactory.getLog(this)

    CfdiSellador33 sellador

    private factory = new ObjectFactory();
    private Comprobante comprobante;
    private Empresa empresa

    private BigDecimal subTotal = 0.0
    private BigDecimal totalImpuestosTrasladados



}
