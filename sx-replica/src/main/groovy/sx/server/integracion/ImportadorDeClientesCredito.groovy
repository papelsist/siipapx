package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.ClienteCredito

/**
 * Created by Luis on 31/05/17.
 */
@Component
class ImportadorDeClientesCredito implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeClientes importadorDeClientes

    def importar(){

        leerRegistros(QUERYCLIENTE,[]).each { row ->
                println "Cliente credito id : "+row.id
            importar(row.id)
        }

    }

    def importar(def sw2){

        def creditoRow=getSql().firstRow(QUERYCREDITO,[sw2])

        println "----"+ creditoRow

        Cliente cliente = Cliente.where {sw2 == creditoRow.cliente_id}.find()
        if(!cliente){
            cliente = importadorDeClientes.importar(creditoRow.cliente_id)
        }
        ClienteCredito credito=ClienteCredito.where {sw2==creditoRow.sw2}.find()

        if(!credito)
            credito =new ClienteCredito()

        credito.cliente=cliente
        credito.cobrador = buscarCobrador(creditoRow.cobrador_id)


        bindData(credito,creditoRow)

        credito.save failOnError:true, flush:true

    }



    static String QUERYCLIENTE="""
        SELECT credito_id as id FROM clientes_credito_integracion
        where credito_id is not null
    """

    static String QUERYCREDITO="""
        SELECT
            CREDITO_ID as sw2,
            c.DESC_ESTIMADO as descuentoFijo,
            c.LINEA as lineaDeCredito,
            c.PLAZO as plazo,
            VENCE_FACTURA as venceFactura,
            c.DIA_REVISION as diaRevision,
            c.DIA_COBRO as diaCobro,
             c.REVISION as revision,
             c.SALDO as saldo,
             ATRASO_MAX as atrasoMaximo,
             c.POSTFECHADO as postFechado,
             c.OPERADOR_CXC as operador,
             (SELECT cliente_id FROM  sx_clientes x where x.CREDITO_ID=c.CREDITO_ID ) as cliente_id,
             (SELECT cobrador_id FROM  sx_clientes x where x.CREDITO_ID=c.CREDITO_ID ) as cobrador_id
        FROM sx_clientes_credito c
        where CREDITO_ID=  ?
    """
}


