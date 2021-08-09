package sx.integracion

import groovy.sql.Sql
import sx.core.Sucursal
import sx.tesoreria.Ficha

class FichasIntegration implements Integracion{

    def validar(Date fecha, Sucursal sucursal) {
        List fichas = getRows(sucursal, fecha)
        def control = fichas.sum( 0.0, {it.total})
        IntegracionLog integracionLog = preparaBitacora(Ficha.class, sucursal, fecha)
        integracionLog.controlDesc = 'total'
        integracionLog.control = control
        integracionLog.registros = fichas.size()
        analisisLocal(integracionLog)
        return integracionLog
    }

    def analisisLocal(IntegracionLog integracionLog){
        List<Ficha> fichas = Ficha.where{fecha == integracionLog.fecha && sucursal == integracionLog.sucursal}.list()
        integracionLog.registrosOficinas = fichas.size()
        integracionLog.controlOficinas = fichas.sum( 0.0, {it.total})
    }
}
