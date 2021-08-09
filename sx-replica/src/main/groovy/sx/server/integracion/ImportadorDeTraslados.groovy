package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.cfdi.Cfdi
import sx.core.Producto
import sx.core.Sucursal
import sx.inventario.SolicitudDeTraslado
import sx.inventario.Traslado
import sx.inventario.TrasladoDet

/**
 * Created by Luis on 01/06/17.
 */
@Component
class ImportadorDeTraslados implements Importador,SW2Lookup {
    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeInventario importadorDeInventario

    @Autowired
    ImportadorDeSolicitudDeTraslado importadorDeSolicitudDeTraslado

    @Autowired
    ImportadorDeCfdi importadorDeCfdi


    def importar(Date f1){

        importar(f1, f1)
    }


    def importar(Date ini, Date fin){

        def ids = leerRegistros("SELECT TRASLADO_ID as traslado_id FROM sx_traslados d  where DATE(fecha) between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            println( r)
            importar(r.traslado_id)
        }
        return 'OK'
    }

    def importar(String traslado_id){

        def movimientos=leerRegistros(QUERY,[traslado_id])
        movimientos.each {trd ->

            importadorDeSolicitudDeTraslado.importar(trd.sol_id)

            SolicitudDeTraslado solicitudDeTraslado=SolicitudDeTraslado.where{sw2==trd.sol_id}.find()

            Traslado traslado=Traslado.where {sw2==trd.sw2}.find()
            Sucursal sucursal=buscarSucursal(trd.sucursal_id)


            if(!traslado){
                traslado=new Traslado()
            } else {
                traslado.partidas.clear()
            }

            traslado.sucursal=sucursal

            println "SolicitudDeTraslado: "+solicitudDeTraslado.id

            traslado.solicitudDeTraslado=solicitudDeTraslado

            if(!trd.porInventario){
                    traslado.porInventario= false
                }

            bindData(traslado,trd)

            importarPartidas(traslado)

            try {
                Cfdi cfdi=importadorDeCfdi.importar(traslado_id)
                traslado.cfdiId=cfdi.id
                traslado.save failOnError:true,flush:true
                importadorDeInventario.crearInventario(traslado,traslado.tipo)


            }catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))

            }


        }
    }

    def importarPartidas(Traslado traslado){
        List partidas = leerRegistros(QUERY_ROW,[traslado.sw2])

        partidas.each{ row ->

            TrasladoDet det =TrasladoDet.where{sw2==row.sw2}.find()
            if(!det){
                det = new TrasladoDet()
            }

            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar(row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto


            bindData(det,row)

            traslado.addToPartidas(det)
        }
    }



    String QUERY="""
    SELECT
        sucursal_id as sucursal_id,
        documento,
        fecha,
        tipo as tipo,
        PORINVENTARIO as porInventario,
        case when comentario is null then CHOFER else concat(COMENTARIO,"--",CHOFER) end as comentario,
        TRASLADO_ID as sw2,
        SOL_ID as sol_id,
        t.CREADO_USR as createUser,
        t.MODIFICADO_USR as updateUser,
        creado  as dateCreated,
        t.MODIFICADO as lastUpdated
    FROM sx_traslados t
    where TRASLADO_ID= ?
    """

    String QUERY_ROW="""
    SELECT
        i.PRODUCTO_ID as producto_id,
        i.CANTIDAD as cantidad,
        i.COMENTARIO as comentario,
        i.INVENTARIO_ID as sw2
    FROM sx_inventario_trd I
    where TRASLADO_ID= ?
    """




}
