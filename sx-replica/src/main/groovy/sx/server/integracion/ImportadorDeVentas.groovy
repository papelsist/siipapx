package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Producto
import sx.core.Venta
import sx.core.VentaDet

/**
 * Created by rcancino on 16/08/16.
 */
@Component
class ImportadorDeVentas implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeVentasCredito importadorDeVentasCredito

    @Autowired
    ImportadorDeVentasCanceladas importadorDeVentasCanceladas

    @Autowired
    ImportadorDeInventario importadorDeInventario

    @Autowired
    ImportadorDeCuentasPorCobrar importadorDeCuentasPorCobrar

    def importar(Date ini, Date fin){


        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha between ? and ? and tipo = ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd'), 'FAC'])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.cargo_id)
        }
        return 'OK'

    }

    def importar(){
        importar(new Date())
    }

    def importar(fecha){
        logger.info("Importando ventas del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha = ? and tipo = ? ",[fecha.format('yyyy-MM-dd'), 'FAC'])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            println "Importando para cargo"+r.cargo_id
            importar(r.cargo_id)
        }
        return 'OK'
    }



    def importar(String sw2){
        String select = QUERY_VENTA + " where tipo = ? and cargo_id = ? "
        def row = findRegistro(select, ['FAC',sw2])

        println "+......................"+sw2

        def venta = build(row)

            println "++++++++++<<<<<<<<<<<<<<<<<<"+venta

        println "+>>>>>>>>>>>>>>>>>>>>>"+sw2

            if(venta.tipo == 'CRE' || venta.tipo == 'PSF') {
                println("importando venta de credito")
               /// importadorDeVentasCredito.importar(venta)
            }

            if(venta.total==0 && venta.tipo!='ANT'){
                println "Importando ventas canceladas"
                importadorDeVentasCanceladas.importarCanc(venta)
            }

        importadorDeCuentasPorCobrar.importar(venta.sw2)

        println "importada la cuenta por cobrar"

        importadorDeInventario.crearInventario(venta,"FAC")

        println "Inventario creado"

        return venta


    }

    def build(def row){

        println('Importando venta con ROW: ' + row)
        def venta = Venta.where{ sw2 == row.sw2}.find()
        if(!venta){
            venta = new Venta()
        }
        bindData(venta,row)
        venta.sucursal = buscarSucursal(row.sucursal_id)
        Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
        if(!cliente){
            println("importando cliente" + row.cliente_id)
            cliente = importadorDeClientes.importar(row.cliente_id)

            println("Cliente importado")
        }
        venta.cliente = cliente
        venta.vendedor = buscarVendedor(row.vendedor_id)
        venta.vale= false
        importarPartidas(venta)

        venta.save failOnError:true, flush:true
        return venta

        try{
           importarPartidas(venta)

            venta.save failOnError:true, flush:true
            return venta

        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }




    }

    def importarPartidas(Venta venta){

        List partidas = leerRegistros(QUERY_PARTIDAS1,[venta.sw2])
        venta.partidas.clear()
        partidas.each{ row ->
            println('Importando partidas: ' + row.sw2)
            VentaDet det = new  VentaDet()
            det.sucursal = buscarSucursal(row.sucursal_id)
            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar( row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }


                det.producto = producto
                det.sucursal=venta.sucursal

                bindData(det,row)

                 println'Agregando partida: ' + row.sw2

                venta.addToPartidas(det)


            println("partida generads")


        }
        return venta
    }


    static String QUERY = """
       select
        cliente_id,
        clave,
        nombre,
        (SELECT c.rfc FROM sx_clientes c where c.CLIENTE_ID=v.CLIENTE_ID) rfc,
        (case when v.ANTICIPO is true then 'ANT'
            when v.POST_FECHADO is true then 'PSF'
            when v.POST_FECHADO is false and v.origen='CRE' then 'CRE'
            when v.origen='CAM' and V.CE is true then 'COD'
            when v.origen='CAM' and V.CE is false then 'CON'
            when v.origen='MOS' then 'CON' else 'ND'  end     ) tipo,
        v.origen,
        v.docto as documento,
        v.fecha fecha,
        (SELECT c.folio FROM sx_cfdi c where c.ORIGEN_ID=v.cargo_id) as folio
        ,(SELECT c.serie FROM sx_cfdi c where c.ORIGEN_ID=v.cargo_id) as serie
        ,(SELECT c.uuid FROM sx_cfdi c where c.ORIGEN_ID=v.cargo_id) as uuid
        ,fecha as facturar
        ,(case when puesto is true then v.creado else null end) as puesto
        ,v.moneda
        ,v.tc as tipoDeCambio
        ,v.importe
        ,v.descuento
        ,v.descuento as descuentoOriginal
        ,v.impuesto
        ,v.total
        ,v.flete as cargosPorManiobra
        ,0.0 as kilos
        ,v.impreso
        ,v.fpago as formaDePago
        ,v.cancelacion_dbf_com as cuentaDePago
        ,0.00 as comisionTarjeta
        ,v.cargos as comisionTarjetaImp
        ,v.vendedor_id
        ,v.comprador
        ,v.sucursal_id
        ,'ND' AS atencion
        ,'SIN_VALE' as clasificacion_vale
        ,v.pedido_fentrega as  forma_de_entrega
        ,v.comentario
        ,v.cargo_id as sw2
        ,FECHA_RECEPCION_CXC as     fechaRecepcionCxc
        ,DIA_DE_REV as     diaRevision
        ,FECHA_REVISION as     fechaRevision
        ,FECHA_REVISION_CXC as     fechaRevisionCxc
        ,PLAZO as     plazo
        ,VTO as     vencimiento
        ,REVISION as     revision
        ,REVISADA as     revisada
        ,DIA_DEL_PAGO as     diaPago
        ,DIA_PAGO as     fechaPago
        ,REPROGRAMAR_PAGO as     reprogramarPago
        ,COMENTARIO_REP_PAGO as     comentarioReprogramarPago
        ,COBRADOR_ID
        ,SOCIO_ID
        from sx_ventas v

    """

    static String QUERY_VENTA="""
    SELECT
        v.cliente_id,
        v.vendedor_id,
        v.sucursal_id,
        v.origen,
        V.docto AS documento,
        v.importe,
        v.impuesto,
        v.total,
        v.descuento,
        v.descuento as descuentoOriginal,
        cargos as cargosPorManiobra,
        0 as comisionTarjeta,
        0 as comisionTarjetaImporte,
        v.FPAGO  as formaDePago,moneda,
        TC as tipoDeCambio,
        ifnull(kilos,0),
        fecha as puesto,
        v.fecha as facturar,
        null as vale,
        null as sucursalVale,
        'SIN_VALE' as clasificacionVale,
        impreso,
        comprador,
        'MOSTRADOR' as atencion,
        'ORDINARIO' as manejoEntrega,
        comentario,
        v.CARGO_ID as sw2,
        fecha,
        creado as dateCreated,
        modificado as lastUpdated,
        CREADO_USERID as createUser,
        CASE WHEN ORIGEN='MOS' THEN 'CON'
            WHEN ORIGEN='CAM' THEN 'COD'
            WHEN ORIGEN ='CRE' THEN 'CRE'
            WHEN POST_FECHADO is true THEN 'PSF'
            WHEN ANTICIPO IS TRUE OR ANTICIPO_APLICADO<>0 THEN 'ANT'
            ELSE 'OTR' END AS tipo
    FROM sx_ventas v

    """

    static String QUERY_PARTIDAS  = """
    select
        D.sucursal_id,
        D.producto_id,
        D.clave,
        D.descripcion,
        D.unidad_id as unidad,
        D.factorU as factor,
        D.nacional,
        D.documento,
        D.fecha,
        D.cantidad,
        D.kilos,
        D.precio_l AS precioOriginal,
        D.precio_l as precioLista,
        D.precio,
        D.importe,
        D.dscto_orig as desctoOriginal,
        D.dscto as descuento,
        D.importe-D.importe_neto AS importeDescuento,
        D.importe_neto as importeNeto,
        D.cortes,
        D.precio_cortes as precioCortes,
        D.cortes*D.precio_cortes as importeCortes,
        D.cortes_Instruccion as cortesInstruccion,
        D.subtotal,
        D.costo as costoReposicion,
        D.costoP as costoPromedio,
        D.costoU as costoUltimo,
        (case when D.CORTES=0 then 'false' else 'true' end) as cortado,
        D.comentario,
        D.inventario_id as sw2
        from sx_ventasdet D
        where venta_id = ?

    """
    static String QUERY_PARTIDAS1 ="""
    SELECT
        producto_id,
        sucursal_id,
        venta_id,
        null as inventario_id,
        documento,
        fecha,
        cantidad,
        precio_l as precioLista,
        precio as precioOriginal,
        precio,
        importe,
        DSCTO as descuento,
        DSCTO_ORIG  as descuentoOriginal,
        0 as importeDescuento,
        importe_neto as importeNeto,
        subtotal,
        nacional,
        kilos,
        comentario,
        CORTES* PRECIO_CORTES as importeCortes,
        INVENTARIO_ID as sw2
    FROM sx_ventasdet d where d.venta_id=?
    """




}
