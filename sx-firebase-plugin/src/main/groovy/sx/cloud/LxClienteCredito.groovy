package sx.cloud

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode


import sx.core.ClienteCredito

@ToString(includeNames=true, includePackage=false)
@EqualsAndHashCode(includes='id')
class LxClienteCredito {

    String  id
    String cliente
    String clave
    String nombre
    Boolean creditoActivo
    Double descuentoFijo
    Double lineaDeCredito
    Long plazo
    Boolean venceFactura
    Long diaRevision
    Long diaCobro
    Boolean revision
    Double saldo
    Long atrasoMaximo
    Boolean postfechado

    Long operador

    Long sw2
    

    public LxClienteCredito() {}

    public LxClienteCredito(ClienteCredito credito) {
        id = credito.id
        cliente = credito.cliente.id
        clave = credito.cliente.clave
        nombre = credito.cliente.nombre
        creditoActivo = credito.creditoActivo
        descuentoFijo = credito.descuentoFijo
        lineaDeCredito = credito.lineaDeCredito
        plazo = credito.plazo
        venceFactura = credito.venceFactura
        diaRevision = credito.diaRevision
        diaCobro = credito.diaCobro
        revision = credito.revision
        saldo = credito.saldo
        atrasoMaximo = credito.atrasoMaximo
        postfechado = credito.postfechado
        operador = credito.operador
    }

    def copyProperties(source, target) {
        def (sProps, tProps) = [source, target]*.properties*.keySet()
        def commonProps = sProps.intersect(tProps) - ['class', 'metaClass', 'additionalMetaMethods', 'socio', 'cobrador', 'cliente']
        commonProps.each { target[it] = source[it] }
    }
    

    Map toMap() {
        Map data = this.properties
        return filter(data)
    }
    Map filter(Map data) {
        data = data.findAll{ k, v -> !['class','constraints', 'errors'].contains(k) }
        return data
    }

    

}
