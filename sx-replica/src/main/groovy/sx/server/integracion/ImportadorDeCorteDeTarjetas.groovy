package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.tesoreria.CorteDeTarjeta

import sx.tesoreria.CorteDeTarjetaAplicacion

/**
 * 
 */
@Component
class ImportadorDeCorteDeTarjetas implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeMovimientosDeCuenta importadorDeMovimientosDeCuenta

    @Autowired
    ImportadorDeCobros importadorDeCobros

    def importar(Date ini, Date fin){
    }

    def importar(fecha){
        logger.info("Importando fichas del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select corte_id from sx_corte_tarjetas where fecha_corte = ? ",[fecha.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.corte_id)
        }
    }

    def importar(Long sw2){
        String select = QUERY + " where corte_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }

    def build(def row){
        def corte = CorteDeTarjeta.where{ sw2 == row.sw2}.find()
        if(corte) return corte
        logger.info('Importando corte de tarjeta ' + row.sw2)
        corte = new CorteDeTarjeta()
        bindData(corte,row)
        corte.sucursal = buscarSucursal(row.sucursal_id)
        corte.cuentaDeBanco = buscarCuentaDeBanco(row.cuenta_id)
        importarPartidas(corte)
        importarAplicaciones(corte)
        corte = corte.save failOnError:true, flush:true

        return corte

    }


    def importarAplicaciones(CorteDeTarjeta corte){
        corte.aplicaciones.clear()
        List partidas = leerRegistros(QUERY_APLICACIONES,[corte.sw2])
        partidas.each{ row ->
            CorteDeTarjetaAplicacion det = new  CorteDeTarjetaAplicacion()
            det.ingreso = importadorDeMovimientosDeCuenta.importar(row.cargoabono_id)
            bindData(det,row)
            det.corte=corte
            corte.addToAplicaciones(det)
        }
    }

    static String QUERY = """
        select
            corte_id as sw2,
            sucursal_id,
            fecha,
            fecha_corte as fechaCorte,
            corte_id as folio,
            tarjeta_tipo as tipoDeTarjeta,
            cuenta_id,
            total,
            comentario,
            creado as dateCreated
            from SX_CORTE_TARJETAS 
    """

    static String QUERY_PARTIDAS = """
        select
        cortedet_id as sw2,
        abono_id
        from sx_corte_tarjetasdet  
        where corte_id = ? 
    """

    static String QUERY_APLICACIONES = """
        select
        corte_id as sw2,
        cargoabono_id,
        comentario,
        importe,
        orden,
        tipo
        from SX_CORTE_TARJETAS_APLICACIONES  
        where corte_id = ? order by orden
    """
}
