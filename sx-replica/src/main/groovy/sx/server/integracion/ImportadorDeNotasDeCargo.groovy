package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.*
//import sx.cxc.ChequeDevuelto
import sx.cxc.NotaDeCargo
import sx.cxc.NotaDeCargoDet
//import sx.tesoreria.Cheque

/**
 * Created by rcancino on 16/08/16.
 */
@Component
class ImportadorDeNotasDeCargo implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeVentas importadorDeVentas

    @Autowired
    ImportadorDeCuentasPorCobrar importadorDeCuentasPorCobrar


  //  @Autowired
  //  ImportadorDeChequesDevueltos importadorDeChequesDevueltos

    def importar(Date ini, Date fin){
        logger.info("Importando notas de cargo" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha between ? and ? and tipo = ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd'), 'CAR'])
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
        logger.info("Importando notas de cargo del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha = ? and tipo = ? ",[fecha.format('yyyy-MM-dd'), 'CAR'])
        logger.info('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.cargo_id)
        }
        return 'OK'
    }

    def importar(String sw2){
        String select = QUERY + " where tipo = ? and cargo_id = ? "
        def row = findRegistro(select, ['CAR',sw2])
        def cargo = build(row)

        importadorDeCuentasPorCobrar.importar(cargo.sw2)

    }

    def build(def row){
        logger.info('Importando Nota de cargo con ROW: ' + row)
        def cargo = NotaDeCargo.where{ sw2 == row.sw2}.find()
        if(!cargo){
            cargo = new NotaDeCargo()
        }
        bindData(cargo,row)
        Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
        if(!cliente){
            cliente = importadorDeClientes.importar(row.cliente_id)
        }


        Sucursal sucursal=buscarSucursal(row.sucursal_id)
        cargo.sucursal=sucursal
        cargo.cliente = cliente
        importarPartidas(cargo)
        try{
            cargo = cargo.save failOnError:true, flush:true
            return cargo
        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }

    }

    def importarPartidas(NotaDeCargo cargo){

        List partidas = leerRegistros(QUERY_PARTIDAS,[cargo.sw2])

        if(cargo.partidas.size()>0)
            cargo.partidas.clear()


        partidas.each{ row ->
            logger.info('Importando partida: ' + row.venta_id)

            NotaDeCargoDet det = new  NotaDeCargoDet()

            if(row.tipo=='CHE'){
                /* ChequeDevuelto chequeDevuelto=ChequeDevuelto.where{sw2==row.venta_id}.find()
                if(!chequeDevuelto){
                    chequeDevuelto=importadorDeChequesDevueltos.importar(row.venta_id)
                }
                */
            }else{
                Venta venta=Venta.where{sw2==row.venta_id}.find()

                if(!venta ){
                    venta=importadorDeVentas.importar(row.venta_id)
                }

                det.venta=venta
            }

            bindData(det,row)
            cargo.addToPartidas(det)
        }


    }



    static String QUERY = """
    SELECT
        cliente_id,
        sucursal_id,
        fecha,
        docto as documento,
        importe,
        impuesto,
        total,
        ifnull(FPAGO,'EFECTIVO') as formaDePago,
        moneda,
        TC as tipoDeCambio,
        comentario,
        CARGOS as cargo,
        CARGO_ID as sw2,
        ifnull(CREADO,fecha) as dateCreated,
        ifnull(MODIFICADO,fecha) as lastUpdated,
        ifnull(CREADO_USERID,'NA') as createUser,
        ifnull(MODIFICADO_USERID,'NA') as updateUser
    FROM sx_ventas
    """

    static String QUERY_PARTIDAS  = """
    SELECT
    d.importe as cargo,
    d.importe,d.venta_id,
    d.CARGODET_ID as sw2,
    v.fecha as dateCreated,
    v.fecha as lastUpdated,
    (SELECT tipo FROM sx_ventas x where x.CARGO_ID=d.VENTA_ID ) as tipo
    FROM sx_notadecargo_det d join sx_ventas v on (v.CARGO_ID=d.CARGO_ID)
    where d.CARGO_ID=?

    """
}
