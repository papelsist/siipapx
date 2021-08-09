package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.cxc.Cobro
import sx.cxc.CobroCheque
import sx.cxc.CobroDeposito
import sx.cxc.CobroTarjeta
import sx.cxc.CobroTransferencia
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco

/**
 * Created by rcancino on 25/10/16.
 */
@Component
class ImportadorDeCobros implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeClientes importadorDeClientes

    def importar(f1,f2){
        (f1..f2).each{
            importar(it)
        }
    }

    def importar(){

        println "Estamos importando de prueba"
    }

    def importar(fecha){
        //logger.info("Importando cobros del : ${fecha.format('dd/MM/yyyy')}" )
        String select = QUERY + " where A.fecha = ? and a.tipo_id like ? order by a.creado"
        def rows = leerRegistros(select,[fecha.format('yyyy-MM-dd'),'PAGO%'])
        def importados = 0
        rows.each { row ->
            build(row)
            importados++
        }
        return importados
    }


    def importar(String sw2){
        //logger.info('Importando cobro ' + sw2)
        String select = QUERY + " and abono_id = ? "
        def row = findRegistro(select, [sw2])
        if(row){
            build(row)
        }

    }

    def build(def row){
        //logger.info('Importando cobro: ' + row)

      //  println "importando cobro: "+ row
        def cobro = Cobro.where{ sw2 == row.sw2}.find()
        if(!cobro){

            println "El cobro no existe procedo a crearlo para :"+row.sw2
            cobro = new Cobro()
            cobro.sucursal = buscarSucursal(row.sucursal_id)
            Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
            if(!cliente){
                println "importando el cliente"+row.cliente_id
                cliente = importadorDeClientes.importar(row.cliente_id)
            }
            cobro.cliente = cliente
            registrarFormaDePago(row,cobro)
        }
        bindData(cobro,row)
        //cobro = cobro.save failOnError:true, flush: true


        try{
            cobro = cobro.save failOnError:true, flush:true
            return cobro
        }catch (Exception ex){
            logger.error(ExceptionUtils.getRootCauseMessage(ex))
        }
    }

    def registrarFormaDePago(def row,Cobro cobro){

        switch (row.tipo_id){
            case 'PAGO_EFE':
                cobro.formaDePago = 'EFECTIVO'
                return cobro
            case 'PAGO_CHE':
               // println "Importando Abono:  "+row.sw2
                cobro.formaDePago = 'CHEQUE'
                CobroCheque cheque = new CobroCheque()
                Banco banco=buscarBanco(row.banco_id)
                cheque.bancoOrigen=banco
                bindData(cheque,row)
               // cheque.save failOnError: true, flush: true
               cobro.cheque = cheque
                return cobro
            case 'PAGO_DEP':
                if(row.totalTransferencia > 0.0){
                    cobro.formaDePago = 'TRANSFERENCIA'
                    CobroTransferencia transferencia = new CobroTransferencia()
                    Banco banco=buscarBanco(row.banco_id)
                    CuentaDeBanco cuenta=buscarCuentaDeBanco(row.cuenta_id)
                    transferencia.cuentaDestino=cuenta
                    transferencia.bancoOrigen=banco
                   bindData(transferencia,row)
                   cobro.transferencia = transferencia
                } else {
                    cobro.formaDePago = 'DEPOSITO'
                    CobroDeposito deposito = new CobroDeposito()
                    Banco banco=buscarBanco(row.banco_id)
                    CuentaDeBanco cuenta=buscarCuentaDeBanco(row.cuenta_id)
                    deposito.cuentaDestino=cuenta
                    deposito.bancoOrigen=banco
                    bindData(deposito,row)
                    cobro.deposito = deposito
                }
                break
            case 'PAGO_TAR':
                cobro.formaDePago = 'TARJETA'
               CobroTarjeta tarjeta= new CobroTarjeta()
                bindData(tarjeta,row)
                cobro.tarjeta=tarjeta
                break
            default:
                cobro.formaDePago = row.tipo_id

        }
        return cobro
    }


    static String QUERY = """
    SELECT
        A.TIPO_ID,
        A.ABONO_ID as sw2,
        A.CLIENTE_ID,
        A.clave,
        A.nombre,
        c.rfc,
        A.fecha,
        A.origen as tipo,
        A.importe,
        A.impuesto,
        A.total,
        A.moneda,
        A.tc as tipoDeCambio,
        A.anticipo,
        A.enviado,
        A.SAF as primeraAplicacion,
        A.diferencia,
        A.DIFERENCIA_FECHA as direrenciaFecha,
        A.COBRADOR_ID,
        A.SUCURSAL_ID,
        A.comentario,
        ifnull(A.numero,0),
        A.postFechado,
        A.VTO as vencimiento ,
        A.CUENTA_HABIENTE as emisor,
        A.CUENTA_CLIENTE as cuenta ,
        A.BANCO as banco,
        A.folio,
        A.cheque as totalCheque,
        A.efectivo as totalEfectivo,
        ifnull(A.transferencia,0) as totalTransferencia,
        0.0 AS totalTarjeta,
        A.fecha_deposito as fechaDeposito,
        A.referencia,
        A.AUTO_TARJETA_BANCO,
        A.comision_tarjeta,
        A.tarjeta_id,
        A.creado as dateCreated,
        'na' as createUser ,
        a.MODIFICADO as lastUpdated,
        'na' as updateUser,
        case when a.tipo_id ='pago_hxe' then 'PAGO_CHE' ELSE a.TIPO_ID end as tipo_id,
        CUENTA_HABIENTE as emisor,
        'NA' as numeroDeCuenta,
        ifnull(a.numero,'') as numero,
        CASE WHEN a.TIPO_ID='PAGO_HXE' THEN true else false end as cambioPorEfectivo,
        (SELECT b.banco_id FROM sw_bancos b where b.clave=a.banco or b.nombre=a.banco) as banco_id,
        a.COMISION_TARJETA as comision,
        (SELECT DEBITO FROM sx_tarjetas t where t.TARJETA_ID=a.TARJETA_ID)  as debitoCredito,
        CUENTA_ID as cuenta_id,
        CHEQUE,
        EFECTIVO
        FROM sx_cxc_abonos A join sx_clientes c on a.cliente_id = c.cliente_id
    """


}
