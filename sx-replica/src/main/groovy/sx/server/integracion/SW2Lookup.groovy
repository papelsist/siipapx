package sx.server.integracion


import sx.core.Cliente
import sx.core.Cobrador
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Sucursal
import sx.core.Vendedor

import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco

/**
 * Created by rcancino on 17/10/16.
 */
trait SW2Lookup {

    Cliente buscarCliente(def siipapId){
        def found = Cliente.where {sw2 == siipapId}.find()
        assert found, "No existe el cliente con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }

    Proveedor buscarProveedor(def siipapId, tipo='COMPRAS'){
        def found = Proveedor.where {sw2 == siipapId && tipo == tipo}.find()
        assert found, "No existe el proveedor con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }

    Producto buscarProducto(def siipapId){
        def found = Producto.where {sw2 == siipapId}.find()
        assert found, "No existe el producto con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }

    Sucursal buscarSucursal(def siipapId) {
        def found = Sucursal.where{sw2 == siipapId}.find()
        assert found, "No existe la sucursal con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }


    Cobrador buscarCobrador(def siipapId) {
        def found = Cobrador.where{sw2 == siipapId}.find()
        assert found, "No existe el cobrador con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }

    Vendedor buscarVendedor(def siipapId) {
        def found = Vendedor.where{sw2 == siipapId}.find()
        assert found, "No existe el vendedor con ID en SW2  $siipapId es probable que no se ha importado"
        return found;
    }

    CuentaDeBanco buscarCuentaDeBanco(def siipapId){
        def found = CuentaDeBanco.where {sw2 == siipapId}.find()
        assert found, "No existe la cuenta de banco con ID en SW2 de: $siipapId, es probable que nose ha importado"
        return found
    }



    Banco buscarBanco(def siipapId){
        def found = Banco.where{sw2==siipapId}.find()
        assert found, "No existe el banco con ID en SW2 de: $siipapId, es probable que nose ha importado"
        return found
    }




}