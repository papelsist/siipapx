package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Producto
import sx.core.Sucursal
import sx.inventario.SolicitudDeTraslado
import sx.inventario.SolicitudDeTrasladoDet
import sx.inventario.Traslado
import sx.inventario.TrasladoDet

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeSolicitudDeTraslado implements  Importador,SW2Lookup {

    @Autowired
    ImportadorDeProductos importadorDeProductos


    def importar(String sol_id){



        def movimientos=leerRegistros(QUERY,[sol_id])

        movimientos.each {sol ->

            SolicitudDeTraslado solicitud=SolicitudDeTraslado.where {sw2==sol.sw2}.find()
            Sucursal sucursal=buscarSucursal(sol.sucursal_id)
            Sucursal atiende=buscarSucursal(sol.origen_id)

            println 'Solicita: '+sucursal
            println 'Atiende: '+atiende

            if(!solicitud){
                solicitud=new SolicitudDeTraslado()
            } else {
                solicitud.partidas.clear()
            }
            solicitud.sucursalSolicita=sucursal
            solicitud.sucursalAtiende=atiende


            bindData(solicitud,sol)

            importarPartidas(solicitud)

            solicitud.save failOnError:true,flush:true

            return  solicitud

        }
    }

    def importarPartidas(SolicitudDeTraslado solicitud){
        List partidas = leerRegistros(QUERY_ROW,[solicitud.sw2])

        partidas.each{ row ->

            SolicitudDeTrasladoDet det =SolicitudDeTrasladoDet.where{sw2==row.sw2}.find()
            if(!det){
                det = new SolicitudDeTrasladoDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto

            bindData(det,row)

            solicitud.addToPartidas(det)
        }
    }



    String QUERY="""
    SELECT
        t.SUCURSAL_ID as sucursal_id,
        t.ORIGEN_ID as origen_id,
        t.DOCUMENTO as documento,
        t.FECHA as fecha,
        t.REFERENCIA as referencia,
        t.CLASIFICACION as clasificacionVale,
        t.NO_ATENDER as noAtender,
        t.comentario as comentario,
        SOL_ID as sw2
    FROM sx_solicitud_traslados as t
    where sol_id = ?
    """

    String QUERY_ROW="""
    SELECT d.PRODUCTO_ID as producto_id,
        d.SOLICITADO as solicitado,
        d.RECIBIDO as recibido,
        d.CORTES as cortes,
        d.CORTES_INSTRUCCION as cortesInstruccion,
        d.COMENTARIO as comentario,
        d.SOL_ID as sw2
    FROM sx_solicitud_trasladosdet d
    where sol_id = ?

    """



}
