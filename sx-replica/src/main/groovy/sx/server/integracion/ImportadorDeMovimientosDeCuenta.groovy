package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import sx.tesoreria.*

/**
 * Created by rcancino on 25/10/16.
 */
@Component
class ImportadorDeMovimientosDeCuenta implements Importador, SW2Lookup{


    def importar(def fecha){
        String select = QUERY + " where fecha=? "

        def rows = leerRegistros(select, [fecha])

        rows.each {row ->
            build(row)
        }


    }


    def importar(Long sw2){
        String select = QUERY + " where CARGOABONO_ID = ? "
        def row = findRegistro(select, [sw2])
        build(row)

    }

    def build(def row){
        logger.info('Importando movimiento: ' + row)
        def movimiento = MovimientoDeCuenta.where{ sw2 == row.sw2}.find()
        if(!movimiento){
            movimiento = new MovimientoDeCuenta()
            movimiento.cuenta = buscarCuentaDeBanco(row.cuenta_id)
        }
        bindData(movimiento,row)
        movimiento = movimiento.save failOnError:true, flush:true
        return movimiento
    }
    

    static String QUERY = """
        SELECT
            a.cargoabono_id as sw2,
            a.cuenta_id,
            a.fecha,
            a.afavor,
            a.`FORMAPAGO` as formaDePago,
            a.origen as tipo,
            a.referencia,
            b.clave as concepto,
            a.comentario,
            a.importe,
            a.moneda,
            a.tc as tipoDeCambio
        from sw_bcargoabono a
        left join sw_conceptos b on (a.concepto_id = b.id)
    """

}
