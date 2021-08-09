package sx.integracion

import groovy.transform.ToString
import sx.core.Sucursal

@ToString(includeFields = true)
class IntegracionLog {

    Date fecha
    Sucursal sucursal
    String sucursalNombre
    String entidad
    BigDecimal registros = 0
    BigDecimal control = 0
    String controlDesc
    String comentario

    BigDecimal registrosOficinas = 0
    BigDecimal controlOficinas = 0

    String info(){
        return " ${entidad} ${sucursal.nombre} ${fecha.format('dd/MM/yyyy')} #Control Sucursal ${control} Oficinas: ${controlOficinas} Dif:${diferencia()}"
    }

    BigDecimal diferencia() {
        return control - controlOficinas
    }

}
