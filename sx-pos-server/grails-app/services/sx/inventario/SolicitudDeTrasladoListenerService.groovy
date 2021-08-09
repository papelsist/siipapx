package sx.audit

import grails.events.annotation.Subscriber


import grails.compiler.GrailsCompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.model.PersistentEntity
import grails.gorm.transactions.Transactional

// import org.hibernate.event.spi.PostUpdateEvent

import sx.inventario.SolicitudDeTraslado
import sx.inventario.TrasladoService
import sx.logistica.Chofer

@Slf4j
@GrailsCompileStatic
@Transactional
class SolicitudDeTrasladoListenerService {

/*
    TrasladoService trasladoService

    String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof SolicitudDeTraslado ) {
            return ((SolicitudDeTraslado) event.entityObject).id
        }
        null
    }

    SolicitudDeTraslado getSolicitud(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof SolicitudDeTraslado ) {
            return (SolicitudDeTraslado) event.entityObject
        }
        null
    }

    @Subscriber
    void afterInsert(PostInsertEvent event) {  
        
        SolicitudDeTraslado solicitud = getSolicitud(event)
        if ( solicitud ) {
            println "************************************Insertando el Sol ${solicitud.id}"
                println "Generando el vale automatico ***************************"
                def chof = Chofer.findBySw2("1")
                   trasladoService.generarTraslados(solicitud.id, chof)
                //    solicitud.atender = new Date()
                //    solicitud.save()
        }
        
    }

    @Subscriber
    void afterUpdate(PostUpdateEvent event) {
        SolicitudDeTraslado solicitud = getSolicitud(event)
        if ( solicitud ) {

        }
    }

    @Subscriber
    void afterDelete(PostDeleteEvent event) {
        SolicitudDeTraslado solicitud = getSolicitud(event)
        if ( solicitud ) {

        }

    }

    void logEntity(SolicitudDeTraslado sol, String type) {
  

    }

    */
}
