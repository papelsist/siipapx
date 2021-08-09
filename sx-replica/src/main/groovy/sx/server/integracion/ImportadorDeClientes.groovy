package sx.server.integracion

import grails.web.databinding.DataBinder
import groovy.sql.Sql
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.ClienteContactos
import sx.core.ComunicacionEmpresa
import sx.core.Direccion
import sx.core.Sucursal


/**
 * Created by rcancino on 19/09/16.
 */
@Component
class ImportadorDeClientes implements  Importador{


    def importar(){
        logger.info('Importando clientes' + new Date().format('dd/MM/yyyy HH:mm:ss'))

        //def hoy = toSqlDate(new Date())
        def importados = 0
        leerRegistros(QUERY,[]).each { row ->

            def cliente = Cliente.where{ sw2 == row.cliente_id}.find()
            if(!cliente){
                cliente = new Cliente()
                importados++
            }
            bindData(cliente,row)
            cliente.direccion = cliente.direccion ?: new Direccion()
            bindData(cliente.direccion,row)

            cliente.save flush:true
        }
        def message = "Clientes importados: $importados"
        return message
    }

    def importarClientesValidos(){
        logger.info('Importando clientes Validos' + new Date().format('dd/MM/yyyy HH:mm:ss'))
        String select=  QUERY_VALIDOS
        def importados = 0
        leerRegistros(select,[]).each { row ->

            println "Importando Cliente"+row.nombre

            def cliente = Cliente.where{ sw2 == row.sw2}.find()
            if(!cliente){
                cliente = new Cliente()
                importados++
            }
            bindData(cliente,row)
            cliente.direccion = cliente.direccion ?: new Direccion()
            bindData(cliente.direccion,row)


            cliente.save failOnError:true, flush:true
        }

    }

    def importar(def sw2,String queryRow){
        def cliente = Cliente.where{ sw2 == sw2}.find() ?: new Cliente()
        def select = queryRow + ' where cliente_id = ?'
        def row = getSql().firstRow(select,[sw2])
        bindData(cliente,row)

        cliente.direccion = cliente.direccion ?: new Direccion()
        bindData(cliente.direccion,row)

        cliente.save failOnError:true, flush:true


    }

    def importar(def sw2){
        def cliente = Cliente.where{ sw2 == sw2}.find() ?: new Cliente()
        def select = QUERY_ROW + ' where cliente_id = ?'
        def row = getSql().firstRow(select,[sw2])
        bindData(cliente,row)
        cliente.direccion = cliente.direccion ?: new Direccion()
        bindData(cliente.direccion,row)

        cliente.save failOnError:true, flush:true



    }



    def importarCatalogoClientes(){


        def queryAudit=""" select * from audit_log_integration where entityName='Cliente' and replicado is null"""


        getSql().eachRow(queryAudit){audit ->
            def queryEntity=""" select * from entity_integration where entity_rx=?  """

            def entity = getSql().firstRow(queryEntity,[audit.entityName])



            if(audit.action.equals("INSERT")){

                println "Importando: "+audit.entityId
               importar(audit.entityId,QUERY_ROW)
                getSql().execute("UPDATE audit_log_integration SET replicado= CURRENT_DATE WHERE ID = :ID",[ID:audit.id]);

            }

            if(audit.action.equals("UPDATE")){
                println "Actualizando"

            }

        }
    }



    def importarColeccionesClientes(Date fecha){

        def clientes=Cliente.findAllByDateCreated(fecha).each {cliente->
            importarColecciones(cliente)
        }
    }


    def importarColecciones(Cliente cliente){


        def contactos=leerRegistros(QUERY_CONTACTO,[cliente.sw2]).each{contactoRow ->


            println contactoRow

            ClienteContactos contacto=ClienteContactos.where {sw2==contactoRow.sw2 && nombre==contactoRow.nombre}.find()

            if(!contacto)
             contacto=new ClienteContactos()

            contacto.cliente=cliente
            contacto.activo=contactoRow.activo
            contacto.email=contactoRow.email1
            contacto.nombre=contactoRow.nombre
            contacto.puesto=contactoRow.puesto
            contacto.sw2=contactoRow.sw2
            contacto.telefono=contactoRow.telefono

            contacto.save failOnError:true, flush:true

        }


        def medios=leerRegistros(QUERY_COMUNICACION,[cliente.sw2,cliente.sw2,cliente.sw2]).each{medioRow ->

                ComunicacionEmpresa medio=ComunicacionEmpresa.where {sw2==medioRow.sw2 && descripcion==medioRow.descripcion }.find()

            if(!medio){
                println "importando Medio de Comunicacion para"+cliente.sw2
                medio =new ComunicacionEmpresa()
            }else{
                println "Actualizando Medio de Comunicacion para"+cliente.sw2
            }

                    medio.cliente=cliente
                    medio.activo=medioRow.activo
                    medio.tipo=medioRow.tipo
                    medio.descripcion=medioRow.descripcion
                    medio.cfdi=medioRow.cfdi
                    medio.sw2=medioRow.sw2


                medio.save failOnError:true, flush:true
        }

    }

    def importarTels(){

        def clientes=Cliente.findAll()

        println("Importando tels")

        clientes.each {cliente ->

            println"Importando tels el cliente "+ cliente.nombre

            def medios=leerRegistros(QUERY_COMUNICACION,[cliente.sw2,cliente.sw2,cliente.sw2]).each{medioRow ->

                ComunicacionEmpresa medio=ComunicacionEmpresa.where {sw2==medioRow.sw2 && descripcion==medioRow.descripcion }.find()

                if(!medio)
                    medio =new ComunicacionEmpresa()

                println "importando Medio de Comunicacion para"+cliente.sw2

                medio.cliente=cliente
                medio.activo=medioRow.activo
                medio.tipo=medioRow.tipo
                medio.descripcion=medioRow.descripcion
                medio.cfdi=medioRow.cfdi
                medio.sw2=medioRow.sw2


                medio.save failOnError:true, flush:true
            }

        }

    }

    static String QUERY = """
            select
            cliente_id as sw2,
            clave,nombre,rfc,
            if(suspendido,'false','true') as activo,
            juridico,
            permitir_cheque as permitirCheque,
            calle,
            numero,
            numeroint,
            delmpo as municipio,
            cp ,
            colonia,
            estado,
            pais
            from sx_clientes
            where year(modificado) > 2015  and month(modificado) > 1

            """
    static String QUERY_ROW = """
            select
            cliente_id as sw2,
            clave,nombre,rfc,
            if(suspendido,'false','true') as activo,
            juridico,
            permitir_cheque as permitirCheque,
            calle,
            numero,
            numeroint,
            delmpo as municipio,
            cp ,
            colonia,
            estado,
            pais
            from sx_clientes

            """

    static String QUERY_CONTACTO="""
            select cliente_id as sw2,true as activo,nombre,puesto,email1,telefono
            from sx_clientes_contactos
            where cliente_id=?
            """

    static String QUERY_COMUNICACION="""
          SELECT
            CLIENTE_ID as sw2,
            true as activo,
            case when tipo like 'TEL%' then 'TEL'
            when tipo like 'CEL%' then 'CEL'
            when tipo like 'FAX%' then 'FAX'
            else 'TEL' end as tipo,
            TELEFONO as descripcion,
            false as cfdi
            FROM sx_clientes_tels
            where cliente_id=?
            union
          SELECT
            CLIENTE_ID as sw2,
            true as ACTIVO,
            'MAIL'aS TIPO,
             EMAIL1 AS DESCRIPCION,
             true as cfdi
          FROM sx_clientes_cfdi_mails where EMAIL1 is not null and EMAIL1 <> '' and cliente_id=?
            union
          SELECT
            CLIENTE_ID as sw2,
            true as ACTIVO,
            'MAIL'aS TIPO,
            EMAIL2 AS DESCRIPCION,
            true as cfdi
          FROM sx_clientes_cfdi_mails where EMAIL2 is not null and EMAIL2 <> '' and cliente_id=?
            """

     static String QUERY_VALIDOS="""
        SELECT
            c.cliente_id as sw2,
            clave,nombre,rfc,
            if(suspendido,'false','true') as activo,
            juridico,
            permitir_cheque as permitirCheque,
            calle,
            numero,
            numeroint,
            delmpo as municipio,
            cp codigoPostal ,
            colonia,
            estado,
            pais
        FROM sx_clientes C JOIN clientes_integracion I ON ( C.CLIENTE_ID=I.CLIENTE_ID)
            """


}
