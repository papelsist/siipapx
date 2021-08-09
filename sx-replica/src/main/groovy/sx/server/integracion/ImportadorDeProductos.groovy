package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.stereotype.Component
import sx.core.Producto
import sx.core.Proveedor
import sx.core.Linea
import sx.core.Clase
import sx.core.Marca

/**
 * Created by rcancino on 16/08/16.
 */
@Component
class ImportadorDeProductos implements Importador{


    def importar(boolean all = false){
        logger.info('Importando productos' + new Date().format('dd/MM/yyyy HH:mm:ss'))

        def importados = 0
        String query = SELECT //+ all ? "" : "  where year(creado) = 2016"
        if(!all) query+= " where year(modificado) = 2017 "
        leerRegistros(query,[]).each { row ->
            try {
                def producto = build(row)
                if(producto){
                    importados++
                    logger.info('Producto importado: '+producto.clave)
                }
            }
            catch(Exception e) {
              println 'Error importando '+ row   
            }
            
        }
        def message = "Productos  importados o actualizados: $importados"
        return message
    }



    def importar(Long id){
        def row = findRegistro(SELECT + " where producto_id = ?", [id])
        build(row)
    }

    def importarProductosValidos(){
        logger.info('Importando productos Validos' + new Date().format('dd/MM/yyyy HH:mm:ss'))
        String select="SELECT producto_id   FROM producto_integracion"
        leerRegistros(select,[]).each { row ->

                println "--------------------"+row.producto_id
            importar(row.producto_id)
        }
    }

    def build(def row){



        def producto = Producto.where{ sw2 == row.sw2}.find()
        
        if(!producto){
            producto = new Producto()
            
        }
        bindData(producto,row)
        producto.linea = Linea.where{ linea == row.linea_id}.find()
        producto.clase = Clase.where{ clase == row.clase_id}.find()
        producto.marca = Marca.where{ marca == row.marca_id}.find()
        if(row.proveedor_id){
            producto.proveedorFavorito = Proveedor.where {sw2 == row.proveedor_id}.find()
        }
        producto = producto.save failOnError:true, flush:true
        return producto
        
    }


    static String SELECT = """
           select
            p.producto_id as sw2,
            p.clave,
            p.descripcion,
            unidad,
            activo,
            kilos,
            gramos,
            caras,
            color,
            acabado,
            presentacion,
            nacional,
            ancho,
            largo,
            deLinea,
            precioContado,
            precioCredito,
            fecha_LP as fechaLista,
            ifnull(modoDeVenta,"B")as modoVenta,
            calibre,
            inventariable,
            l.nombre as linea_id,
            c.nombre as clase_id,
            m.nombre as marca_id,
            p.ajuste,
            p.proveedor_id
            from sx_productos p
            left join sx_lineas l on(l.LINEA_ID = p.linea_id)
            left join sx_marcas m on(m.marca_id = p.marca_id)
            left join sx_clases c on(c.clase_id = p.clase_id)
            """

    static String WHERE = """
            where year(creado) = 2016
            """
}

