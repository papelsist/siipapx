package sx.server.integracion


import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import sx.core.Cliente
import sx.core.Producto
import sx.core.PreciosPorCliente
import sx.core.PreciosPorClienteDet



@Component
class ImportadorLPCliente implements  Importador{

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeProductos importadorDeProductos


    def importar(){

        leerRegistros(QUERY,[]).each { row ->
            def lista = PreciosPorCliente.where{ sw2 == row.sw2}.find()
            if(!lista){
                lista = new PreciosPorCliente()

            }
            bindData(lista,row)
            Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
            if(!cliente){
                println("importando cliente" + row.cliente_id)
                cliente = importadorDeClientes.importar(row.cliente_id)
                println("Cliente importado")
            }
            lista.cliente = cliente

            importarPartidas(lista)

            lista.save(failOnError:true, flush:true)

        }

    }

    def importarPartidas(PreciosPorCliente lista){

        List partidas = leerRegistros(QUERY_PARTIDAS,[lista.sw2])
        lista.partidas.clear()
        partidas.each{ row ->
            PreciosPorClienteDet det = new  PreciosPorClienteDet()

            Producto producto = Producto.where {sw2 == row.producto_id}.find()


            if(!producto) {
                println("Importando producto $row.producto_id para la venta")
                producto = importadorDeProductos.importar( row.producto_id)
                assert producto, 'No fue posible importar el producto :' +row.producto_id
            }

            det.producto = producto

            bindData(det,row)

            lista.addToPartidas(det)

        }

    }


    static String QUERY = """
        SELECT UUID(),0 AS VERSION,DESCUENTO_FIJO as descuento,now() as date_created,VIGENTE as activo,'' as comentario,now() as last_updated,lista_id as sw2,CLIENTE_ID as cliente_id
        FROM sx_lp_cliente WHERE VIGENTE IS TRUE
    """

    static String QUERY_PARTIDAS = """
        SELECT PRECIOU as precio,clave,moneda,costo,1 as tipo_de_cambio,pre
        cio_lista as precio_de_lista,descuento,costop,PRODUCTO_ID as producto_id,descripcion,0 as precio_por_kilo,costou
        FROM sx_lp_cliente_det where lista_id=?
        """





}
