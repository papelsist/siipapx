    package sx.cxc

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Cliente
import sx.core.Sucursal
import sx.tesoreria.AutorizacionDeDeposito
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco

@ToString(excludes = ["id,cliente,sucursal,efectivo,cheque,transferencia total"],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class SolicitudDeDeposito {

    String	id

    Sucursal sucursal

    Cliente cliente

    Cobro	cobro

    Banco banco

    CuentaDeBanco cuenta

    String	tipo = 'NORMAL'

    Integer	folio = 0

    Date	fecha

    Date	fechaDeposito

    String	referencia

    BigDecimal	cheque	 = 0.0

    BigDecimal	efectivo = 0.0

    BigDecimal	transferencia = 0.0

    BigDecimal	total = 0.0

    String	comentario

    Date	cancelacion

    String	cancelacionComentario

    Boolean	enviado	 = false

    String sw2

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    AutorizacionDeDeposito autorizacion

    static mapping={
        id generator: 'uuid'
        fechaDeposito type: 'date'
        fecha type: 'date'
    }

    static constraints = {
        sw2 nullable: true
        cobro nullable: true
        cancelacion nullable: true
        cancelacionComentario nullable: true
        comentario nullable: true
        createUser nullable: true
        updateUser nullable: true
        autorizacion nullable: true
    }
}
