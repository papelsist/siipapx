package sx.server.integracion

import org.springframework.stereotype.Component
import sx.cxc.VentaCredito
import sx.core.Socio

/**
 * Created by Luis on 30/05/17.
 */

@Component
class ImportadorDeVentasCredito implements Importador, SW2Lookup {


        def importar(venta){
            println "Importando ventaCredito"
            def credito = VentaCredito.findByVenta(venta)

            if(!credito)
                credito = new VentaCredito()

            def vCred = findRegistro( QUERY_CREDITO , [venta.sw2])

            credito.cobrador = buscarCobrador(vCred.cobrador_id)
            credito.venta=venta
            bindData(credito , vCred)

            def socio = Socio.where{sw2 == vCred.socio_id}.find()

            if(socio){
                credito.socio=socio
            }

            credito.save failOnError:true, flush:true


        }

    static String QUERY_CREDITO="""
    SELECT v.plazo,
        v.vto as vencimiento,
        v.FECHA_RECEPCION_CXC as fechaRecepcionCxc,
        v.DIA_DE_REV as diaRevision,
        FECHA_REVISION as fechaRevision,
        v.FECHA_REVISION_CXC as fechaRevisionCxc,
        v.DESCUENTO as descuento,
        v.REVISION as revision,
        v.REVISADA as revisada,
        v.DIA_DEL_PAGO as diaPago,
        v.DIA_PAGO as fechaPago,
        v.REPROGRAMAR_PAGO as reprogramarPago,
        v.COMENTARIO_REP_PAGO as comentarioReprogramarPago,
        v.COBRADOR_ID as cobrador_id,
        v.SOCIO_ID as socio_id,
        cargo_id as sw2,
        creado as dateCreated,
        modificado as lastUpdated,
        CREADO_USERID as createUser,
        CREADO_USERID as updateUser
    FROM sx_ventas v where CARGO_ID=?

    """
}
