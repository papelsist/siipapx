package com.luxsoft

import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.springframework.context.ApplicationEvent
import grails.core.GrailsDomainClass
import sx.core.AuditLog
import sx.cxc.Cobro
import sx.cxc.CobroCheque
import sx.cxc.CobroDeposito
import sx.cxc.CobroTarjeta
import sx.cxc.CobroTransferencia
import sx.cxc.SolicitudDeDeposito

@Slf4j
class CustomAuditLogListener extends AbstractPersistenceEventListener {

def dataSource

    protected CustomAuditLogListener(Datastore datastore) {
        super(datastore)
    }

    @Override
    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {

        switch(event.eventType) {
            case EventType.PostInsert:
                eventRegister(event,"INSERT")
                break
            case EventType.PostUpdate:
                eventRegister(event,"UPDATE")
                break;
            case EventType.PostDelete:
                eventRegister(event,"DELETE")
                break;
        }
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return true
    }

    static GrailsDomainClass getDomainClass(domain) {
        if (domain && Holders.grailsApplication.isDomainClass(domain.class)) {
            Holders.grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domain.class.name) as GrailsDomainClass
        } else {
            null
        }
    }

    def eventRegister(AbstractPersistenceEvent event,String eventName){
        try{
            def domain = event.entityObject
            def entity = getDomainClass(domain)
            def sql=new Sql(dataSource)
            def row=sql.firstRow("select * from entity_replicable where name=? and replicable is true",[entity.name])
            if(row){
                def audit = new AuditLog()
                audit.name = entity.name
                audit.tableName = row.table_name
                audit.persistedObjectId = domain.id?.toString()
                audit.eventName = eventName
                audit.source = domain.hasProperty('sucursal') && domain.sucursal ? domain.sucursal.nombre :"NA"
                audit.target = 'CENTRAL'
                asignarSucursalDestino(audit, domain)
                saveAuditLog(audit)
            }
        }catch (Exception ex){
            log.error('Error al generar auditlog {}, {}', event, ExceptionUtils.getRootCauseMessage(ex))
        }
    }

    def saveAuditLog(AuditLog audit){
        Date now = new Date()
        AuditLog.withNewSession {
            try {
                audit.save(flush: true, failOnError: true)
                // log.debug('AuditLog generado: {}', audit)
            }
            catch (e) {
                log.error "Error generando auditlog:  {} Ex:{}", audit, ExceptionUtils.getRootCauseMessage(e)
            }
        }
    }



    def asignarSucursalDestino(AuditLog audit, Object domain) {
        switch (audit.name) {
            case SolicitudDeDeposito.class.simpleName:
            case Cobro.class.simpleName:
                audit.target = domain.sucursal.nombre
                break
            case CobroTransferencia.class.simpleName:
            case CobroCheque.class.simpleName:
            case CobroDeposito.class.simpleName:
            case CobroTarjeta.class.simpleName:
                audit.target = domain.cobro.sucursal.nombre
                break
            default:
                return
        }
    }

}
