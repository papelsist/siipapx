package sx.bi

import sx.core.Sucursal


class VentaPorFacturista {
    Date fecha
    Sucursal sucursal
    String sucursalNom
    String createUser
    String facturista

    Integer con
    Integer cod
    Integer cre
    Integer canc
    Integer devs
    Integer facs
    BigDecimal importe
    Integer partidas
    Integer pedFact
    Integer ped
    Integer pedMosCON
    Integer pedMosCOD
    Integer pedMosCRE
    Integer pedTelCON
    Integer pedTelCOD
    Integer pedTelCRE

    static constraints = {
        fecha unique: ['facturista', 'sucursalNom']
    }

    static mapping = {
        fecha type:'date', index: 'COMPRA_IDX1'
    }
}

