package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by Luis on 08/06/17.
 */
@Component
class ImportadorAOperacionesAlmacen implements Importador, SW2Lookup {

    @Autowired
    ImportadorMovimientoAlmacen importadorMovimientoAlmacen
    @Autowired
    ImportadorDeTransFormaciones importadorDeTransFormaciones
    @Autowired
    ImportadorDeTraslados importadorDeTraslados
    @Autowired
    ImportadorDeDevolucionDeVentas importadorDeDevolucionDeVentas
    @Autowired
    ImportadorDeRecepcionDeCompras importadorDeRecepcionDeCompras
    @Autowired
    ImportadorDevolucionCompra importadorDevolucionCompra
    @Autowired
    ImportadorDeCompras importadorDeCompras

    def importar(def fecha){

        println "Importando movimientos  "+fecha
        importadorMovimientoAlmacen.importar(fecha)

        println "Importando transformaciones"
        importadorDeTransFormaciones.importar(fecha)

        println "Importando Traslados"
        importadorDeTraslados.importar(fecha)

        println "Importando Dev Ventas"
        importadorDeDevolucionDeVentas.importar(fecha)

        println "Importando Recepcion Compras"
        importadorDeRecepcionDeCompras.importar(fecha)

        println "Importando Devolucion Compras"
        importadorDevolucionCompra.importar(fecha)

        println "Importando Compras"
        importadorDeCompras.importar(fecha)
    }



}
