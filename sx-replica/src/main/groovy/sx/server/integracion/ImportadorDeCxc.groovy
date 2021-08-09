package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ImportadorDeCxc implements Importador, SW2Lookup {

    @Autowired ImportadorDeCobros importadorDeCobros

    @Autowired ImportadorDeChequesDevueltos importadorDeChequesDevueltos

    @Autowired ImportadorDeCorteDeTarjetas importadorDeCorteDeTarjetas

    @Autowired ImportadorDeCuentasPorCobrar importadorDeCuentasPorCobrar

    @Autowired ImportadorDeNotasDeCargo importadorDeNotasDeCargo

    @Autowired ImportadorDeAplicacionDeCobro importadorDeAplicacionDeCobro


    def importar(fecha){

        println "Importando Cuentas por Cobrar"


        println "Importando Cobros"
        importadorDeCobros.importar(fecha)

        println "Importando Cheques Devueltos"
        importadorDeChequesDevueltos.importar(fecha)

        println "Importando Corte de Tarjetas"
        importadorDeCorteDeTarjetas.importar(fecha)

        println "Importando Cuentas Por Cobrar"
        importadorDeCuentasPorCobrar.importar(fecha)

        println "Importando Notas de Cargo"
        importadorDeNotasDeCargo.importar(fecha)

        println "Importando Aplicaciones de Cobro"
        importadorDeAplicacionDeCobro.importar(fecha)

    }
}

