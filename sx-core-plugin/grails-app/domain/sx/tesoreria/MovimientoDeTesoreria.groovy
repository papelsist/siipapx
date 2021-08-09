package sx.tesoreria


/**
 * Entidad que agrupa los movimientos de bancos  agenos a las operaciones basicas de la empresa
 * como pueden ser Aclaraciones,Conciliaciones,Faltantes Sobrantes
 *
 * Created by rcancino on 06/04/17.
 */
class MovimientoDeTesoreria {

    String id
    

    Date fecha

    ConceptoTesoreria concepto

    BigDecimal importe

    CuentaDeBanco cuenta

    MovimientoDeCuenta movimiento

    String comentario

    Date dateCreated
    Date lastUpdated

    static constraints = {
        comentario nullable: true
    }

    static mapping = {
        id generator: 'uuid'
        fecha type: 'date'

    }
}

enum ConceptoTesoreria {
    ACLARACION,
    CONCILIACION,
    FALTANTE,
    SOBRANTE
}