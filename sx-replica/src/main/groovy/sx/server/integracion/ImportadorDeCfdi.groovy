package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.stereotype.Component
import sx.cfdi.Cfdi


/**
 * Created by Luis on 14/06/17.
 */

@Component
class ImportadorDeCfdi implements Importador, SW2Lookup{


    def importar(def sw2){
        logger.info('Importando Cfdi ' + sw2)
        String select = QUERY + " where  origen_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)

    }

    def build(def row){
        Cfdi cfdi=Cfdi.where{sw2==row.sw2}.find()

        if(!cfdi){
            cfdi =new Cfdi()
        }

        bindData(cfdi,row)

        try{
            cfdi.save failOnError:true, flush:true
        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }

    }



     static String QUERY="""
    SELECT
        emisor,
        rfc as receptorRfc,
        'PAP830101CCR3' as emisorRfc,
        case when TIPO_CFD='I' then 'INGRESO'
            WHEN TIPO_CFD ='E' THEN 'EGRESO'
            WHEN TIPO_CFD='T' THEN 'TRASLADO' END as tipoDeComprobante,
        XML_FILE as fileName,
        uuid,
        serie,
        CREADO  as fecha,
        folio,
        TOTAL as total,
        CREADO as dateCreated,
        MODIFICADO as lastUpdated,
        cfd_id as sw2,
        'http://www.papelsa.com.mx' as url,
        ifnull((SELECT ifnull(FPAGO,'EFECTIVO') FROM sx_ventas v where v.cargo_id=c.ORIGEN_ID ),'EFECTIVO') as formaDePago,
        'PUE' AS metodoDePago
    FROM sx_cfdi c
    """
}
