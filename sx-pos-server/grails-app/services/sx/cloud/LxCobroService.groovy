package sx.cloud

import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import javax.annotation.PostConstruct

import com.google.firebase.cloud.*
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Query
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.DocumentSnapshot

import com.google.cloud.firestore.ListenerRegistration 
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FirestoreException;

import com.google.cloud.firestore.EventListener;
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import grails.gorm.transactions.Transactional
import grails.util.Environment


import sx.core.AppConfig
import sx.core.Sucursal
import sx.core.Cliente
import sx.cxc.SolicitudDeDeposito
import sx.cxc.Cobro
import sx.cxc.CobroTransferencia
import sx.cxc.CobroDeposito
import sx.core.Cliente
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco


@Slf4j
@Transactional
class LxCobroService implements ApplicationListener<ContextRefreshedEvent>, EventListener<QuerySnapshot>  {

    static lazyInit = false

    FirebaseService firebaseService
    ListenerRegistration registration
    def suc

    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
        start()
    }

    static String TIME_FORMAT = 'dd/MM/yyyy HH:mm'


    def cobroCreate(DocumentChange documentChange) {
 
       // println "Generando el cobro desde el documento change"

        def suc = AppConfig.first().sucursal

        DocumentSnapshot depositoSnapshot =documentChange.getDocument()
        Map<String,Object> deposito = depositoSnapshot.getData() 
            depositoFbToDeposito(deposito, depositoSnapshot)
    }

    def depositoFbToDeposito(Map<String,Object> deposito, def depositoSnapshot){

        def solicitud = crearSolicitudDeposito(deposito)
         
        def cobro = crearCobro(solicitud)
        
        if(cobro){  
             
            Map<String, Object> update = new HashMap<>();
            update.put("estado", 'ATENDIDO');          
            DocumentReference depositoRef = depositoSnapshot.getReference()
            ApiFuture<WriteResult> writeResult = depositoRef.set(update, SetOptions.merge()) 
        }
        
  

    }
    
    def crearSolicitudDeposito(Map<String,Object> depositoFb){
         
        if(depositoFb){
             
            Cliente cliente = Cliente.get(depositoFb.cliente.id) 
            Banco banco = Banco.get(depositoFb.banco.id)
            CuentaDeBanco cuenta = CuentaDeBanco.get(depositoFb.cuenta.id)
            def suc = AppConfig.first().sucursal
           
            SolicitudDeDeposito sol = new SolicitudDeDeposito()    
            sol.sucursal =  suc
            sol.cliente = cliente
            sol.banco = banco
            sol.cuenta = cuenta
            sol.folio = depositoFb.folio
            sol.total = depositoFb.total
            // sol.fecha = depositoFb.fecha
            sol.fecha = Date.parse("yyyy-MM-dd'T'HH:mm:ss", depositoFb.fecha)
            //sol.fechaDeposito = depositoFb.fechaDeposito
            sol.fechaDeposito =  Date.parse("yyyy-MM-dd'T'HH:mm:ss", depositoFb.fechaDeposito)
            sol.referencia = depositoFb.referencia
            sol.comentario = 'AUTORIZADO'
            sol.sw2 = depositoFb.id
            if(depositoFb.transferencia){
                sol.transferencia = depositoFb.total            
            }
            if(depositoFb.importes.cheque > 0){
                sol.cheque =  depositoFb.importes.cheque  
            }
            if(depositoFb.importes.efectivo > 0){
                sol.efectivo =  depositoFb.importes.efectivo  
            }
            if(depositoFb.importes.tarjeta > 0){
                sol.tarjeta =  depositoFb.importes.tarjeta 
            }
           
            sol.save failOnError:true, flush:true
            return sol
        }

    }

    def crearCobro(SolicitudDeDeposito solicitud){
        println "Generando el cobro"
        if(solicitud){
            Cobro cobro = new Cobro()
            cobro.cliente = solicitud.cliente
            cobro.sucursal = solicitud.sucursal
            cobro.tipo = 'CON'
            cobro.fecha = solicitud.fecha
            cobro.importe = solicitud.total
            cobro.referencia = solicitud.referencia
            if(solicitud.transferencia > 0){
                cobro.formaDePago = 'TRANSFERENCIA'
                CobroTransferencia transf = new CobroTransferencia()
                transf.bancoOrigen = solicitud.banco
                transf.cuentaDestino = solicitud.cuenta
                transf.folio = solicitud.folio
                transf.fechaDeposito = solicitud.fechaDeposito
                transf.cobro = cobro
                cobro.transferencia = transf
            }
            if(solicitud.cheque > 0 ){
                cobro.formaDePago = 'DEPOSITO_CHEQUE'
                CobroDeposito depo = new CobroDeposito()
                depo.bancoOrigen = solicitud.banco
                depo.cuentaDestino = solicitud.cuenta
                depo.folio = solicitud.folio
                depo.fechaDeposito = solicitud.fechaDeposito
                depo.totalCheque = solicitud.cheque
                depo.cobro = cobro
                cobro.deposito = depo
            }
            if(solicitud.efectivo > 0){
                cobro.formaDePago = 'DEPOSITO_EFECTIVO'
                CobroDeposito depo = new CobroDeposito()
                depo.bancoOrigen = solicitud.banco
                depo.cuentaDestino = solicitud.cuenta
                depo.folio = solicitud.folio
                depo.fechaDeposito = solicitud.fechaDeposito
                depo.totalEfectivo = solicitud.efectivo
                depo.cobro = cobro
                cobro.deposito = depo
            }
            
            cobro.save failOnError:true, flush:true
            solicitud.cobro = cobro
            solicitud.save failOnError:true, flush:true
        }
    }

    void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException ex) {
        if(ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error("Error: {}", msg, ex)
        }
        // log.info('Changes detected: {}', snapshots)
        snapshots.getDocumentChanges().each { DocumentChange dc ->
            log.info('Document: {} Type: {}', dc.getDocument().getData().folio, dc.type)
            switch (dc.type) {
                case 'ADDED':
                    //println('Se agrego un deposito: ')              
                        cobroCreate(dc)
                    break
                case 'MODIFIED':
                    // println('Se modifico un deposito ') 
                    break
                case 'REMOVED':
                    // println('Se elimino un deposito: ')    
                    break
            }
        }
        
    }

    def start() {
        suc = AppConfig.first().sucursal
        Firestore db = firebaseService.getFirestore()
        registration = db.collection('depositos')
        .whereEqualTo('sucursal', suc.nombre)
        .whereEqualTo('cerrado', true)
        .whereEqualTo('estado', 'AUTORIZADO')
        .addSnapshotListener(this)
        log.debug('Listening to firestore collection: {}', 'depositos')
    }   

    @PreDestroy
    def stop(){
        if(registration) {
            registration.remove()
            log.debug('Firbase listener for collection: {} removed' , 'depositos')
        }
    }

    def createAudit(){

    }

        //@PostConstruct
    /* def listenerRegistration(){

        ListenerRegistration registration

    
        def sucursal = AppConfig.first().sucursal

        Firestore db = firebaseService.getFirestore()

		registration = db.collection("depositos").whereEqualTo('estado', 'AUTORIZADO').whereEqualTo('sucursal', sucursal.nombre).whereEqualTo('atendido', false)
				.addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			void onEvent(
					@Nullable QuerySnapshot snapshots,
					@Nullable FirestoreException ex) {
				if(ex) {
                    println "Error"
					String msg = ExceptionUtils.getRootCauseMessage(ex)
					println("Error: {}", msg, ex)
				}
				snapshots.getDocumentChanges().each { DocumentChange dc ->

					switch (dc.type) {
						case 'ADDED':
							println('Se agrego un deposito: ')
                            cobroCreate(dc)
							break
						case 'MODIFIED':
                            println('Se modifico un deposito Existente')
                            // cobroCreate(dc)
							break
						case 'REMOVED':
							println('Se elimino un deposito')
							break
					}

				}
			}
		})
    } */ 

}
