package sx.tesoreria

import sx.sat.BancoSat

class CuentaDeBanco {

    String id

    BancoSat bancoSat

    String numero

    String clave

    String descripcion

    String tipo

    Currency moneda

    Boolean activo = true

    Boolean disponibleEnVenta = false

    String subCuentaOperativa

    String impresionTemplate

    Long sw2

    Date dateCreated

    Date lastUpdated

    static constraints = {
    	numero maxSize:30
        clave maxSize:30, unique: true
        descripcion minSize:3
        tipo inList:['CHEQUES','INVERSION']
        impresionTemplate nullable:true, maxSize:50
        subCuentaOperativa nullable:true, maxSize:4
        sw2 nullable:true
        bancoSat nullable: true
    }

    String toString() {
        return "$descripcion (${numero})"
    }

    static mapping={
        id generator:'uuid'
    }
}
