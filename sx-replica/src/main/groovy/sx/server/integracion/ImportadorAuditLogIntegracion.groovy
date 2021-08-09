package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by Luis on 07/06/17.
 */

@Component
class ImportadorAuditLogIntegracion implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeExistencia importadorDeExistencia

    @Autowired
    ImportadorDeProductos importadorDeProductos

    @Autowired
    ImportadorDeProveedores importadorDeProveedores

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeClientesCredito importadorDeClientesCredito

    def importar(){
        def registros=leerRegistros("select id,entityId,entityName,action from audit_log_integration where replicado is null and message is null",[])

        registros.each { row ->

            try{
                switch(row.entityName){

                    case 'Existencia':
                        importadorDeExistencia.importar(row.entityId)
                        break

                    case 'Cliente' :
                        importadorDeClientes.importar(row.entityId)
                        break

                    case 'Producto' :
                        importadorDeProductos.importar(row.entityId)
                        break

                    case 'Proveedor' :
                        importadorDeProveedores.importar(row.entityId,"COMPRAS")
                        break

                    case 'ClienteCredito' :
                        importadorDeClientesCredito.importar(row.entityId)

                    default :
                        break

                }

                getSql().execute("UPDATE audit_log_integration SET replicado= CURRENT_TIMESTAMP , message=null WHERE  ID = :ID",[ID:row.id]);

            }catch(Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e))

                getSql().execute("UPDATE audit_log_integration SET message='Error' WHERE ID = :ID",[ID:row.id]);
            }

        }

    }



}
