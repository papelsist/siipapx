package sx.server.integracion.carga

import org.springframework.stereotype.Component
import sx.core.Clase
import sx.core.Cobrador
import sx.core.Direccion
import sx.core.Linea
import sx.core.Marca
import sx.core.Socio
import sx.core.Sucursal
import sx.core.Vendedor
import sx.server.integracion.Importador
import sx.server.integracion.SW2Lookup
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco

/**
 * Created by rcancino on 06/09/16.
 */
@Component
class ImportadorDeCatalogosBasicos implements Importador, SW2Lookup{

    def importar(){
        importarLineas()
        .importarMarcas()
        .importarClases()
        .importarSucursales()
        .importarVendedores()
        .importarCobradores()
        .importarBancos()
        .importadorDeCuentasBancarias()
        //.importarSocios()
    }

    def importarLineas(){
        def query = "select * from sx_lineas"
        leerRegistros(query,[]).each { row ->
            def linea = Linea.findOrSaveWhere(linea: row.nombre)
        }
        return this
    }

    def importarMarcas(){
        def query = "select * from sx_marcas"
        leerRegistros(query,[]).each { row ->
            Marca.findOrSaveWhere(marca: row.nombre)
        }
        return this
    }

    def importarClases(){
        def query = "select * from sx_clases"
        leerRegistros(query,[]).each { row ->
            Clase.findOrSaveWhere(clase: row.nombre)
        }
        return this
    }
    

    def importarSucursales(){
        def query = """
        select clave,nombre,habilitada as activa, sucursal_id as sw2,calle,numero,calle,numero,
        numeroint,delmpo as municipio,cp as codigoPostal,colonia,estado,pais from sw_sucursales
        """
        leerRegistros(query,[]).each { row ->
            def sucursal = Sucursal.findByClave(row.clave)
            if(!sucursal){
                sucursal = new Sucursal(row)
                sucursal.direccion = resolveDireccion(row)
                sucursal.save failOnError:true, flush:true
            }
        }
        return this
    }

    def resolveDireccion(row){
        return new Direccion(
                calle:row.calle,
                numeroInterior: row.numeroInt,
                numeroExterior: row.numero,
                colonia: row.colonia,
                municipio: row.municipio,
                estado: row.estado,
                pais: row.pais,
                codigoPostal: row.codigoPostal
        )
    }

    

    def importarCobradores(){
        def query = "select * from sx_cobradores"
        leerRegistros(query,[]).each { row ->
            def cobrador = Cobrador.where {sw2 == row.id}.find()
            if(cobrador == null) cobrador = new Cobrador()
            cobrador.nombres = row.nombres
            cobrador.comision = row.comision
            cobrador.activo = row.activo
            cobrador.sw2 = row.id
            cobrador.save failOnError: true, flush:true
        }
        return this
    }

    def importarVendedores(){
        def query = "select * from sx_vendedores"
        leerRegistros(query,[]).each { row ->
            def vendedor = Vendedor.where {sw2 == row.clave}.find()
            if(vendedor == null) vendedor = new Vendedor()
            vendedor.nombres = row.nombres
            vendedor.comisionContado = row.comision_con
            vendedor.comisionCredito = row.comision
            vendedor.activo = row.activo
            vendedor.sw2 = row.clave
            vendedor.save failOnError: true, flush:true
        }
        return this
    }

    def importarSocios(){
        def query = "select * from sx_socios"
        leerRegistros(query,[]).each { row ->
            def socio = Socio.where {sw2 == row.socio_id}.find()
            if(socio == null) socio = new Socio()
            socio.sw2 = row.socio_id
            bindData(socio,row)
            socio.cliente = buscarCliente(row.cliente_id)
            socio.vendedor = buscarVendedor(row.vendedor_id)
            socio.save failOnError: true, flush:true
        }
        return this
    }

    def importarBancos(){
        def query = "select banco_id, clave as nombre from sw_bancos"
        leerRegistros(query,[]).each { row ->
            def banco = Banco.where {sw2 == row.banco_id}.find()
            if(banco == null) banco = new Banco()
            banco.sw2 = row.banco_id
            bindData(banco,row)
            banco.save failOnError: true, flush:true
        }
        return this
    }

    def importadorDeCuentasBancarias(){
        def query = "select id as cuenta_id, clave, numero, moneda, descripcion, tipo, banco_id from sw_cuentas"
        leerRegistros(query,[]).each { row ->
            def cuenta = CuentaDeBanco.where {sw2 == row.cuenta_id}.find()
            if(cuenta == null) cuenta = new CuentaDeBanco()
            cuenta.sw2 = row.cuenta_id
            bindData(cuenta,row)
            cuenta.banco = Banco.where { sw2 == row.banco_id }.find()
            cuenta.save failOnError: true, flush:true
        }
        return this
    }
}
