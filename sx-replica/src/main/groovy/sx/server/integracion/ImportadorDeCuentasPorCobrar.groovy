package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Venta
//import sx.cxc.ChequeDevuelto
import sx.cxc.CuentaPorCobrar

import sx.cxc.NotaDeCargo

/**
 * Created by rcancino on 16/08/16.
 */
@Component
class ImportadorDeCuentasPorCobrar implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeVentas importadorDeVentas

    @Autowired
    ImportadorDeCfdi importadorDeCfdi


    def importar(Date ini, Date fin){
        logger.info("Importando cuentas por cobrar del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha between ? and ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.cargo_id)
        }
    }

    def importar(fecha){
        logger.info("Importando cuentas por cobrar del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha = ? ",[fecha.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.cargo_id)
        }
    }

    def importar(String sw2){
        logger.info('Importando Cuenta Por cobrar ' + sw2)
        String select = QUERY + " where  cargo_id = ? "
        def row = findRegistro(select, [sw2])
        def cuentaPorCobrar=build(row)

        if(cuentaPorCobrar && (cuentaPorCobrar.tipoDocumento!='CHEQUE_DEVUELTO' && cuentaPorCobrar.tipoDocumento!='DEVOLUCION_CLIENTE')){
            importadorDeCfdi.importar(sw2)
        }

    }

    def build(def row){

        CuentaPorCobrar cuentaPorCobrar=CuentaPorCobrar.where{sw2==row.sw2}.find()

        if(!cuentaPorCobrar){
            cuentaPorCobrar =new CuentaPorCobrar()
        }

        bindData(cuentaPorCobrar,row)
        cuentaPorCobrar.sucursal = buscarSucursal(row.sucursal_id)
        Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
        if(!cliente){
            cliente = importadorDeClientes.importar(row.cliente_id)
        }
        cuentaPorCobrar.cliente = cliente



        try{
            cuentaPorCobrar.save failOnError:true, flush:true
            if(cuentaPorCobrar.tipoDocumento=='VENTA'){
                Venta venta=Venta.where{sw2==cuentaPorCobrar.sw2}.find()
                venta.cuentaPorCobrar=cuentaPorCobrar
                venta.save failOnError: true, flush: true

            }
            if(cuentaPorCobrar.tipoDocumento=='NOTA_DE_CARGO'){
                NotaDeCargo notaDeCargo=NotaDeCargo.where{sw2==cuentaPorCobrar.sw2}.find()
                notaDeCargo.cuentaPorCobrar=cuentaPorCobrar
                notaDeCargo.save failOnError: true, flush: true

            }
            if(cuentaPorCobrar.tipoDocumento=='CHEQUE_DEVUELTO'){
               // ChequeDevuelto cheque=ChequeDevuelto.where{sw2==cuentaPorCobrar.sw2}.find()
              //  cheque.cuentaPorCobrar=cuentaPorCobrar
               // cheque.save failOnError: true, flush: true
            }


            return cuentaPorCobrar

        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
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
        IFNULL(FPAGO,'EFECTIVO') as formaDePago,
        moneda,
        CARGOS as cargo,
        comentario,
        IFNULL(creado,FECHA) as dateCreated,
        IFNULL(MODIFICADO,FECHA) as lastUpdated,
        IFNULL(CREADO_USERID,'NA') as createUser,
        IFNULL(MODIFICADO_USERID,'NA') as updateUser,
        case when tipo='FAC' THEN 'VENTA'
            WHEN TIPO='CHE' THEN 'CHEQUE_DEVUELTO'
            WHEN TIPO='CAR' THEN 'NOTA_DE_CARGO'
            WHEN TIPO='TES'  THEN 'DEVOLUCION_CLIENTE' END AS tipoDocumento,
        CARGO_ID as sw2,(select uuid from sx_cfdi c where c.ORIGEN_ID= v.cargo_id ) as uuid
    FROM sx_ventas v

    """

}
