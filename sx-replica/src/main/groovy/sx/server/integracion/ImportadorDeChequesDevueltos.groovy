package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.*
import sx.core.Cliente
import sx.core.Sucursal
//import sx.cxc.ChequeDevuelto
import sx.cxc.Cobro
import sx.cxc.CobroCheque

/**
 * Created by Luis on 20/06/17.
 */

@Component
class ImportadorDeChequesDevueltos implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeCobros importadorDeCobros

    @Autowired
    ImportadorDeCuentasPorCobrar importadorDeCuentasPorCobrar

    def importar(Date ini, Date fin){
        println("Importando Cheques devueltos" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha between ? and ? and tipo = ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd'), 'CHE'])
        println('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.cargo_id)
        }
        return 'OK'
    }

    def importar(){
        importar(new Date())
    }

    def importar(fecha){
        println("Importando Cheques devueltos del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha = ? and tipo = ? ",[fecha.format('yyyy-MM-dd'), 'CHE'])
        println('Registros: ' + ids.size())

        ids.each { r ->

            println ">>>>"+r.cargo_id
            importar(r.cargo_id)
        }
        return 'OK'
    }

    def importar(String sw2){
        String select = QUERY + " where tipo = ? and cargo_id = ? "
        def row = findRegistro(select, ['CHE',sw2])
        def cheque = build(row)

        if(cheque){
            importadorDeCuentasPorCobrar.importar(cheque.sw2)
        }



    }

    def build(def row){
        /*
        println('Importando Cheque devuelto con ROW: ' + row)
        def cheque = ChequeDevuelto.where{ sw2 == row.sw2}.find()
        if(!cheque){
            cheque = new ChequeDevuelto()
        }
        bindData(cheque,row)
        Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
        if(!cliente){
            cliente = importadorDeClientes.importar(row.cliente_id)
        }

        Cobro cobro=Cobro.where{sw2==row.cheque_id}.find()

        if(!cobro){
            importadorDeCobros.importar(row.cheque_id)
        }
        CobroCheque cobroCheque=CobroCheque.where{sw2==row.cheque_id}.find()
        Sucursal sucursal=buscarSucursal(row.sucursal_id)
        cheque.sucursal=sucursal
        cheque.cliente = cliente
        cheque.cheque=cobroCheque

        try{
            cheque= cheque.save failOnError:true, flush:true
            return cheque
        }catch (Exception ex){
            println(ExceptionUtils.getRootCauseMessage(ex))
        }
        */
    }


    static String QUERY="""
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
        ifnull(MODIFICADO_USERID,'NA') as updateUser,
        cheque_id
    FROM sx_ventas
    """

}
