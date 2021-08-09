package com.luxsoft


import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Commons
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.springframework.context.ApplicationEvent
import grails.core.GrailsDomainClass
import sx.core.AuditLog

@Commons
class CustomAuditLogListener extends AbstractPersistenceEventListener {

def dataSource

    protected CustomAuditLogListener(Datastore datastore) {
        super(datastore)
    }


    @Override
    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {


        switch(event.eventType) {
/*
           case EventType.PreInsert:
                println "PRE INSERT ${event.entityObject}"
                break
*/
            case EventType.PostInsert:
                //println "POST INSERT ${event.entityObject}"

                eventRegister(event,"INSERT")

                break
/*
            case EventType.PreUpdate:
                println "PRE UPDATE ${event.entityObject}"
                break
*/
            case EventType.PostUpdate:
                //println "POST UPDATE ${event.entityObject}"

                eventRegister(event,"UPDATE")

                break;
/*
           case EventType.PreDelete:
                println "PRE DELETE ${event.entityObject}"

                break;
*/
            case EventType.PostDelete:
               // println "POST DELETE ${event.entityObject}"

                eventRegister(event,"DELETE")

                break;
/*
            case EventType.PreLoad:
                println "PRE LOAD ${event.entityObject}"


                break;

            case EventType.PostLoad:
                println "POST LOAD ${event.entityObject}"
                 break;
*/
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


    def saveAuditLog(AuditLog audit){
        audit.with {
            dateCreated = lastUpdated = new Date()
        }
        try {
            //def auditLog=getDomainClass(audit)
            audit.withNewSession {

                audit.merge(flush: true, failOnError: true)

            }
        }
        catch (e) {
            log.error "Failed to create  for ${audit}: ${e.message}"
        }
    }

    def eventRegister(def event,String eventName){

        try{
            def domain = event.entityObject

            def entity = getDomainClass(domain)

            def sql=new Sql(dataSource)

            def row=sql.firstRow("select * from entity_replicable where name=? and replicable is true",[entity.name])

            if(row){

                def audit=new AuditLog()

                audit.name =entity.name
                audit.tableName=row.table_name
                audit.persistedObjectId = domain.id?.toString()
                audit.eventName =eventName
                audit.source =domain.hasProperty('sucursal') && domain.sucursal ? domain.sucursal.nombre :"NA"
                audit.target ='CENTRAL'

                saveAuditLog(audit)
            }

        }catch (e){
            log.error "Error for register event on : ${event.entityObject} ", e
        }

    }

}
