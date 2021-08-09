package sx.server.integracion.importacion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.server.integracion.Importador
import sx.server.integracion.ImportadorDeCobros
import sx.server.integracion.ImportadorDeMovimientosDeCuenta
import sx.server.integracion.SW2Lookup
import sx.tesoreria.Ficha
import sx.tesoreria.FichaDet

/**
 * 
 */
@Component
class ImportadorDeFichas implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeMovimientosDeCuenta importadorDeMovimientosDeCuenta

    @Autowired
    ImportadorDeCobros importadorDeCobros

    def importar(Date ini, Date fin){
    }

    def importar(fecha){
        logger.info("Importando fichas del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select ficha_id from SX_FICHAS where fecha = ? ",[fecha.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.ficha_id)
        }
    }

    def importar(String sw2){
        //logger.info('Importando ficha  ' + sw2)
        String select = QUERY + " where ficha_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }

    def build(def row){
        def ficha = Ficha.where{ sw2 == row.sw2}.find()
        if(ficha) logger.info('Actualizando ficha ' + row.sw2)
        if(!ficha){
            logger.info('Importando ficha ' + row.sw2)
            ficha = new Ficha()
            bindData(ficha,row)
            ficha.sucursal = buscarSucursal(row.sucursal_id)
            ficha.cuentaDeBanco = buscarCuentaDeBanco(row.cuenta_id)
        }
        if(row.cargoabono_id) {
            logger.info('Vinculando movimiento de cuenta: ' + row.cargoabono_id)
            ficha.ingreso = importadorDeMovimientosDeCuenta.importar(row.cargoabono_id)
        }
        importarPartidas(ficha)
        ficha = ficha.save failOnError:true, flush:true
        return ficha

    }

    def importarPartidas(Ficha ficha){
        ficha.partidas.clear()
        List partidas = leerRegistros(QUERY_PARTIDAS,[ficha.sw2])
        partidas.each{ row ->
            FichaDet det = new  FichaDet()
            if(row.abono_id) {
                det.cobro = importadorDeCobros.importar(row.abono_id)
            }
            bindData(det,row)
            ficha.addToPartidas(det)
        }
    }

    static String QUERY = """
        select
            ficha_id as sw2,
            sucursal_id,
            cargoabono_id,
            fecha,
            folio,
            tipo_ficha as tipoDeFicha,
            cuenta_id,
            evalores as envioForaneo,
            origen,
            fecha_corte as fechaCorte,
            total,
            comentario,
            creado as dateCreated
            from SX_FICHAS 
    """

    static String QUERY_PARTIDAS = """
        select
        fichadet_id as sw2,
        banco,
        cheque,
        efectivo,
        abono_id
        from SX_FICHASDET  
        where ficha_id = ? order by renglon
    """
}
