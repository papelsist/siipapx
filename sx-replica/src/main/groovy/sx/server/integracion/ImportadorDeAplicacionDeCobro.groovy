package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Venta
import sx.cxc.AplicacionDeCobro
import sx.cxc.Cobro
import sx.cxc.CuentaPorCobrar

/**
 * Created by rcancino on 01/11/16.
 */
@Component
class ImportadorDeAplicacionDeCobro implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeCobros importadorDeCobros

    @Autowired
    ImportadorDeVentas importadorDeVentas


    def importar(f1,f2){
        (f1..f2).each{
            importar(it)
        }
    }

    def importar(fecha){
        logger.info("Importando cobranza  del : ${fecha.format('dd/MM/yyyy')}" )
        String select = QUERY + " where A.fecha = ? and tipo='PAGO' "
        def rows = leerRegistros(select,[fecha.format('yyyy-MM-dd')])
        def importados = 0
        rows.each { row ->
            if(row.car_tipo=='FAC'){
                build(row)
            }else{
                println "La aplicacion no es una aplicacion de factura"
            }
            importados++
        }
        return importados
    }


    def importar(String sw2){
        logger.info('Importando cobro ' + sw2)
        String select = QUERY + " where aplicacion_id = ? "
        def row = findRegistro(select, [sw2])
        if(row.car_tipo=='FAC'){
            build(row)
        }else{
            println "La aplicacion no es una aplicacion de factura"
        }


    }

    def build(def row){
        logger.info('Importando aplicacion de cobro: ' + row)
        def aplicacion = AplicacionDeCobro.where { sw2 == row.sw2}.find()

        Cobro cobro =Cobro.where{sw2==row.abono_id}.find()
        Venta venta=Venta.where{sw2==row.cargo_id}.find()

        if(!cobro){
            cobro=importadorDeCobros.importar(row.abono_id)
        }
        if(!venta){
            venta=importadorDeVentas.importar(row.cargo_id)
        }



        if(!aplicacion){
            aplicacion = new AplicacionDeCobro()

        }

        println "Venta "+venta

        println "Cobro"+cobro

        aplicacion.cobro=cobro
        aplicacion.cuentaPorCobrar=venta.cuentaPorCobrar

        bindData(aplicacion,row)
        aplicacion.save failOnError:true, flush:true

        try{



        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }
    }

    static QUERY = """
    SELECT
        abono_id,
        cargo_id,
        fecha,
        importe,
        APLICACION_ID as sw2,
        CREADO as dateCreated,
        MODIFICADO as lastUpdated,
        'NA' AS createUser,
        'NA'as updateUser,
        car_tipo
    FROM  sx_cxc_aplicaciones a
    """
}
