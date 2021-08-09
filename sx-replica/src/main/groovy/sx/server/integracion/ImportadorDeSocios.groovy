package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Socio
import sx.core.Vendedor

/**
 * Created by Luis on 31/05/17.
 */

@Component
class ImportadorDeSocios implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeClientes importadorDeClientes

    def importar(){
        leerRegistros(QUERY,[]).each { row ->
            println "Socio: " + row.sw2

            Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
            Vendedor vendedor=buscarVendedor(row.vendedor_id)

            if(!cliente){
                cliente = importadorDeClientes.importar(row.cliente_id)
            }

            Socio socio=Socio.where {sw2==row.sw2}.find()

            if(!socio)
                socio=new Socio()

            socio.cliente=cliente
            socio.vendedor=vendedor

            bindData(socio,row)

            socio.save failOnError:true,flush:true

        }
    }



    static String QUERY="""
    SELECT s.clave as clave,
        s.nombre as nombre,
        s.comisionCobrador as comisionCobrador,
        s.comisionVendedor as comisionVendedor,
        s.VENDEDOR_ID as vendedor_id,
        s.CLIENTE_ID as cliente_id,
        s.direccion as direccion,
        s.SOCIO_ID as sw2
    FROM sx_socios s
    """
}
