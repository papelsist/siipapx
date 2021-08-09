package sx.cloud

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.annotation.Nullable

import groovy.util.logging.Slf4j

import org.springframework.stereotype.Component

import grails.compiler.GrailsCompileStatic
import grails.web.databinding.DataBinder
import grails.gorm.transactions.Transactional
import grails.util.Environment

import org.apache.commons.lang3.exception.ExceptionUtils

import com.google.cloud.firestore.*
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.*
import com.google.firebase.cloud.*
import static com.google.cloud.firestore.DocumentChange.Type.*

import sx.core.Cliente


@Slf4j
// @GrailsCompileStatic
class LxClienteListenerService implements DataBinder, EventListener<QuerySnapshot> {
	
	static lazyInit = false

	FirebaseService firebaseService

	final static String COLLECTION = 'clientes_log'

	ListenerRegistration registration

	@PostConstruct
	def start() {
		log.info('Registering listener to firebase collection: {}', COLLECTION)
		Firestore db = firebaseService.getFirestore()
		registration = db.collection(COLLECTION)
		.addSnapshotListener(this)
		log.info('Listening to firestore collection: {}', COLLECTION)
	}

	// @PreDestroy
	def stop() {
		if(registration) {
			registration.remove()
			log.info('Firbase listener for collection: {} removed' , COLLECTION)
		}
	}

	void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException ex) {
		if(ex) {
			String msg = ExceptionUtils.getRootCauseMessage(ex)
			log.error("Error: {}", msg, ex)
		}
		
		if(snapshots.getDocumentChanges().size() == 1) {
			DocumentChange dc = snapshots.getDocumentChanges()[0]
			log.debug('Document change: {}', dc)
			QueryDocumentSnapshot document = dc.document
			switch (dc.type) {
				case ADDED:
				case MODIFIED:
					updateCliente(document.data)
					break
				case REMOVED:
					break
			}
		}
	}

	@Transactional()
	void updateCliente(Map data) {

		log.debug('Actualizando cliente {} ', data.clienteId)
		try {

			
			Cliente target = Cliente.get(data.clienteId)
			
			// bindData(filteredData, data, excludes: ['direcciones'])
			log.info('Actualizando cliente: {}', target.clave)
			
			// Solo actualizamos medios de cotacto
			List medios = data.medios
			medios.each {
				log.info('Actualizando: {}', it)
			}
		} catch (Exception ex) {
			log.error('Exception: {}', ex.message)
			
		}
	}
}


