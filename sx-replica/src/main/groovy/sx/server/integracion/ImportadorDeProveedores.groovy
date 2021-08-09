package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Direccion
import sx.core.Producto
import sx.core.Proveedor
import sx.core.ProveedorCompras
import sx.core.ProveedorProducto

/**
 * Created by rcancino on 06/09/16.
 */
@Component
class ImportadorDeProveedores implements Importador{

    @Autowired
    ImportadorDeProductos importadorDeProductos
/*
    def importar(String tipo = 'COMPRAS', String clave){
        String query = tipo == 'COMPRAS' ? QUERY_COMPRAS : QUERY_GASTOS
        query += ' where clave = ?'
        def row = findRegistro(query, [clave])
        Proveedor proveedor = build(row)
    }
*/
    def importar(Long sw2, tipo = 'COMPRAS'){
        logger.info(" Importando proveedor ${sw2} (${tipo})")
        String sql = tipo == 'COMPRAS' ? QUERY_COMPRAS : QUERY_GASTOS
        sql+= ' where proveedor_id=?'
        def row = findRegistro(sql, [sw2])
        build(row)
    }

    def importar(String tipo){
        if(!tipo) tipo = 'COMPRAS'
        println("Importando proveedores (${tipo})")


        String query = tipo == 'COMPRAS' ? QUERY_COMPRAS : QUERY_GASTOS

        int importados = 0
        def errores = []

        leerRegistros(query,[]).each { row ->

            build(row)
            importados++
            try {
               // build(row)
                //importados++
            }
            catch(Exception e) {
                errores.add(row.sw2)
            }
            
        }

        def message = "Proveedores importados: $importados (${tipo})"
        if(errores){
            message + "Errores importando proveedores: "+ errores.join(',')
        }
        return message
    }



    def build(def row){
        logger.info('Importando proveedor: ' + row)
        def proveedor = Proveedor.where{sw2 == row.sw2 && tipo == row.tipo}.find()
        if(!proveedor){
            proveedor = new Proveedor()
        }
        proveedor.rfc = proveedor.rfc ?:'XAXX010101000'
        proveedor.direccion = resolveDireccion(row)
        bindData(proveedor,row)
        //proveedor = proveedor.save failOnError:true, flush:true
        try{
            proveedor = proveedor.save failOnError:true, flush:true
            importarProveedorCompras(proveedor)
        }catch(Exception e) {
           e.printStackTrace()
        }

        return proveedor
    }


    def importarProveedorCompras(Proveedor proveedor){

        if(proveedor.tipo=='COMPRAS'){

            def rowCompras = getSql().firstRow(QUERY_PROV_COMPRAS,[proveedor.sw2])
            ProveedorCompras provCompras=ProveedorCompras.where {proveedor==proveedor}.find()
            if(!provCompras){
                provCompras=new ProveedorCompras()
            }
            provCompras.proveedor=proveedor
            if(!rowCompras.cuentaContable)
                provCompras.cuentaContable='NA'
            bindData(provCompras,rowCompras)

            provCompras.save failOnError:true,flush:true

        }

        return proveedor

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

    def importarProveedorProductos(){
        String query = "select * from sx_proveedor_productos order by proveedor_id"
        leerRegistros(query, []).each {  row ->
            def proveedor = Proveedor.where{sw2==row.proveedor_id && tipo =='COMPRAS'}.find() //.findBySw2(row.proveedor_id)

            Producto producto = Producto.where {sw2 == row.producto_id}.find()
           /*
            if(!producto){
                producto = importadorDeProductos.importar(row.producto_id)
            }
            */
            if(producto){

                ProveedorProducto provProd = ProveedorProducto.where {proveedor == proveedor && producto == producto}.find()
                if(!provProd){
                    provProd = new ProveedorProducto(proveedor:proveedor, producto:producto)
                }
                provProd.proveedor=proveedor
                provProd.claveProveedor = row.claveprov
                provProd.codigoProveedor = row.codigoprov
                provProd.descripcionProveedor = row.descriprov
                provProd.paqueteTarima = row.paqtarima
                provProd.piezaPaquete = row.piezapaq
                provProd.save failOnError:true, flush: true

            }

        }
        def message = "Productos por proveedor importados"
        return message
    }


    def importarProveedorProductos(def prov){
        String query = "select * from sx_proveedor_productos where proveedor_id=? "
        leerRegistros(query, [prov]).each {  row ->
            println  row
            def proveedor = Proveedor.where{sw2==row.proveedor_id && tipo =='COMPRAS'}.find() //.findBySw2(row.proveedor_id)


            println "**************************"+proveedor+"********************************************"


            Producto producto = Producto.where {sw2 == row.producto_id}.find()
            /*
             if(!producto){
                 producto = importadorDeProductos.importar(row.producto_id)
             }
             */
            if(producto){
                println "**********************************************************************"+"---------------"
                ProveedorProducto provProd = ProveedorProducto.where {proveedor == proveedor && producto == producto}.find()

                println "+++++++++++"+provProd

                if(!provProd){
                    provProd = new ProveedorProducto(proveedor:proveedor, producto:producto)
                }
                provProd.proveedor=proveedor
                provProd.claveProveedor = row.claveprov
                provProd.codigoProveedor = row.codigoprov
                provProd.descripcionProveedor = row.descriprov
                provProd.paqueteTarima = row.paqtarima
                provProd.piezaPaquete = row.piezapaq


                provProd.save failOnError:true, flush: true

            }

        }
        def message = "Productos por proveedor importados"
        return message
    }

    static String QUERY_COMPRAS = """
        select nombre,clave,
        if(ifnull(rfc,'') = '', 'XAXX010101000', rfc) as rfc,
        proveedor_id as sw2,
        activo,
        'COMPRAS' as tipo,
        telefono1,
        telefono2,
        telefono3,
        calle,
        numero,
        numeroint,
        delmpo as municipio,
        cp as codigoPostal,
        colonia,
        estado,
        pais,
        email1,
        email2
        from sx_proveedores 
    """
    static String QUERY_GASTOS = """
        select 
        ifnull(nombre,concat(apellidop,' ',apellidom,' ', nombres)) as nombre,
        'GASTOS' as tipo,
        concat('G-',proveedor_id) as clave,
        if(ifnull(rfc,'') = '', 'XAXX010101000', rfc) as rfc,
        proveedor_id as sw2,
        'GASTOS' as tipo
        ,telefono1,
        telefono2,
        telefono3,
        calle,
        numero,
        numeroint,
        delmpo as municipio,
        cp as codigoPostal,
        colonia,
        estado,
        pais,
        email1,
        email2
        from sw_gproveedor 
    """
    static String QUERY_PROV_COMPRAS="""
        SELECT
            ifnull(p.CUENTACONTABLE,'') as cuentaContable,
            p.DESCUENTOF as descuentoF,
            p.DIASDF as diasDF,
            p.plazo as plazo,
            p.VTO_FECHAREV as fechaRevision,
            p.IMPRIMIR_COSTO as imprimirCosto
        FROM sx_proveedores p
        where proveedor_id=?
    """
}
