package papws.api
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import grails.util.Environment

import org.springframework.beans.factory.annotation.Value

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.WriteResult
import com.google.api.core.ApiFuture

import com.google.firebase.messaging.FirebaseMessaging


@Slf4j
@CompileStatic
class CloudService {

  private FirebaseApp papelws

  @PostConstruct()
  init() {
    String dirPath = '.'
    String fileName = 'papx-ws-prod-firebase-sdk.json'
    if(Environment.current == Environment.DEVELOPMENT) {
      dirPath = System.getProperty('user.home') + '/.firebase'
      fileName = 'papx-ws-dev-firebase-sdk.json'
    }
    File file = new File(dirPath, fileName)
    FileInputStream serviceAccount = new FileInputStream(file);
    log.debug('Inicializando Firebase services para PapelWS Credentials: {}', file.path )
    try {
      FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()
      this.papelws = FirebaseApp.initializeApp(options, 'papelws')
    } catch( Exception ex) {
      log.error(ex.message, ex)
    }
  }

  Firestore getFirestore() {
    return FirestoreClient.getFirestore(this.papelws)
  }

  FirebaseApp getApp() {
    return this.papelws
  }

  /**
   * Registra errores en Firestore
   */
  void logError(Map payload) {
    try {
      log.info('Registrando error en Firebase {}', payload)

      ApiFuture<WriteResult> result = getFirestore()
        .collection('errors')
        .document()
        .set(payload)

      def updateTime = result.get().getUpdateTime().toDate().format('dd/MM/yyyy')
      log.debug('Logged error at : {}', updateTime)
    } catch (Exception ex) {
      log.error('No se pudo regisrar el error en Firebase message: {}', ex.message);
    }
  }


  @PreDestroy()
  void close() {
    if(this.papelws) {
      String appName = this.papelws.name
      this.papelws.delete()
      this.papelws = null
      log.debug('Papel firebase ws  {} disconected', appName)
    }
  }
}
