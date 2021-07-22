package papws.audit

import grails.compiler.ast.GrailsDomainClassInjector
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.springframework.context.ApplicationEvent
import grails.core.GrailsDomainClass

import sx.core.AuditLog

import javax.sql.DataSource


@Slf4j
class AuditlogListener extends AbstractPersistenceEventListener {

  DataSource dataSource
  Map<String,Boolean> tables

  protected AuditlogListener(Datastore datastore) {
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
        break
      case EventType.PostDelete:
        eventRegister(event,"DELETE")
        break
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


  @Transactional
  void eventRegister(def event,String eventName){

    try {
      Object domain = event.entityObject
      GrailsDomainClass entity = getDomainClass(domain)
      if(this.getTables().get(entity.name)) {

        AuditLog audit=new AuditLog()
        audit.name =entity.name
        audit.tableName=row.table_name
        audit.persistedObjectId = domain.id?.toString()
        audit.eventName =eventName
        audit.source =domain.hasProperty('sucursal') && domain.sucursal ? domain.sucursal.nombre :"NA"
        audit.target ='CENTRAL'
        audit.dateCreated = new Date()
        audit.save flush: true
      }
    }catch (e){
      log.error "Error for register event on : ${event.entityObject} ", e
    }
  }

  Map<String, Boolean> getTables() {
    if(!tables) {
      Sql sql=new Sql(dataSource)
      List rows = sql
        .rows("select table_name from entity_replicable where replicable is true")
      this.tables = rows.collectEntries {
        // [it.get('table_name') : Boolean.TRUE]
        [(it.get('table_name')): true]
      }
    }
    return this.tables
  }

}
