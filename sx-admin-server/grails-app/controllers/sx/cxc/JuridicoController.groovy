package sx.cxc

import grails.gorm.transactions.Transactional
import grails.rest.*
import groovy.util.logging.Slf4j

@Slf4j
class JuridicoController extends RestfulController<Juridico> {

    static responseFormats = ['json']

    JuridicoController() {
        super(Juridico)
    }

    @Override
    protected List<Juridico> listAllResources(Map params) {
        // log.info('List: {}', params)
        return super.listAllResources(params)
    }

    @Override
    protected Juridico createResource() {
        Juridico juridico = new Juridico()
        bindData juridico, getObjectToBind()
        CuentaPorCobrar cxc = juridico.cxc
        juridico.saldo = cxc.saldo
        juridico.importe = cxc.total
        juridico.nombre = cxc.cliente.nombre
        // log.info('Jur: {}', juridico)
        return juridico
    }

    @Override
    protected Juridico saveResource(Juridico resource) {
        log.info('Salvando juridico: {}', resource)
        CuentaPorCobrar cxc = resource.cxc
        cxc.juridico = resource.traspaso
        cxc.save flush: true
        resource.save failOnError: true, flush: true
        // resource.save flush: true
    }

    @Transactional
    def mandarFacturas(MandarJuridicoCommand command) {
        if(command == null) {
            notFound()
            return
        }
        log.info('Juridico: {}', command)
        List<Juridico> rows = []
        command.facturas.each { cxc ->
            Juridico juridico = new Juridico()
            juridico.cxc = cxc
            juridico.saldo = cxc.saldo
            juridico.importe = cxc.total
            juridico.nombre = cxc.cliente.nombre
            juridico.traspaso = command.traspaso
            juridico.comentario = command.comentario
            juridico.abogado = command.abogado
            juridico.despacho = command.despacho
            juridico.save failOnError: true, flush: true
            cxc.juridico = juridico.traspaso
            cxc.save flush: true
            rows << juridico
        }
        respond rows
    }
}


class MandarJuridicoCommand {
    DespachoDeCobranza despacho
    String abogado
    String comentario
    List<CuentaPorCobrar> facturas
    Date traspaso

    static constraints = {
        comentario nullable: true
    }

    String toString() {
        "${this.facturas.size()} Facturas para al despacho: ${this.despacho}  Fecha: ${traspaso.format('dd/MM/yyyy')}"
    }
}