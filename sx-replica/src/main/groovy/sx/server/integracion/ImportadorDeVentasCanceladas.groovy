package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Venta
import sx.core.VentaCancelada
import sx.cxc.Cobro

import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.commons.lang.builder.ToStringStyle

/**
 * Created by rcancino on 26/10/16.
 */
@Component
class ImportadorDeVentasCanceladas implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeVentas importadorDeVentas

    def importar(f1,f2){
        (f1..f2).each{
            importar(it)
        }
    }

    def importar(fecha){
        logger.info("Importando ventas canceladas : ${fecha.format('dd/MM/yyyy')}" )
        String select = QUERY_CANC + " where date(c.CREADO) = ? and v.tipo = 'FAC' "
        def ids = leerRegistros(select,[fecha.format('yyyy-MM-dd')])
        def importados = 0
        ids.each { row ->
            build(row)
            importados++
        }
        return importados
    }


    def importar(String sw2){
        String select = QUERY_CANC + " where c.id = ? and v.tipo = 'FAC' "
        println select
        def row = findRegistro(select, [sw2])
        if(row)
            build(row)
    }

    def importarCanc(Venta venta){

        def row = findRegistro(QUERY_CANC,[venta.sw2])
        if(row){
            def cancelada = VentaCancelada.where{ sw2 == row.sw2}.find()

            if(!cancelada)
                cancelada = new VentaCancelada()

            bindData(cancelada,row)
            cancelada.venta=venta
            println ToStringBuilder.reflectionToString(cancelada, ToStringStyle.SHORT_PREFIX_STYLE)

            cancelada.save failOnError:true, flush:true
            println ToStringBuilder.reflectionToString(cancelada, ToStringStyle.SHORT_PREFIX_STYLE)

        }

    }

    def build(def row){
        logger.info('Importando venta cancelada: ' + row)
        def cancelada = VentaCancelada.where{ sw2 == row.sw2}.find()
        if(!cancelada){
            cancelada = new VentaCancelada()

            bindData(cancelada,row)
            Venta venta = Venta.where {sw2 == row.venta_id}.find()
            if(!venta){
                venta = importadorDeVentas.importar(row.venta_id)
            }
            assert venta, 'No se ha importado la venta: ' + row.venta_id
            cancelada.venta = venta
            try{
                cancelada = cancelada.save failOnError:true, flush:true
                return cancelada
            }catch (Exception ex){
                logger.error(ExceptionUtils.getRootCauseMessage(ex))
            }
        }
    }

    static QUERY = """
    SELECT
        c.fecha,
        c.creado_userid as creadoUserid,
        c.comentario,
        c.id as sw2,
        c.cargo_id as venta_id,
        C.AUT_ID as autorizacion
    FROM sx_cxc_cargos_cancelados c
    join sx_ventas v on(v.cargo_id=c.cargo_id)
    """
    static String QUERY_CANC="""
    SELECT
        c.cargo_id,
        c.fecha,
        c.comentario,
        c.AUT_ID as autorizacion,
        ifnull(c.MODIFICADO_USERID,'') as usuario,
        c.ID as sw2,
        c.CREADO as dateCreated,
        ifnull(c.MODIFICADO,c.CREADO) as lastUpdated
    FROM sx_cxc_cargos_cancelados c
    where c.cargo_id=?
    """
}

