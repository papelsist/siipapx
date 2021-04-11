package sx.cloud.papws

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

import com.google.firebase.messaging.FirebaseMessaging


@Slf4j
class PapelsaProdCloudService {
  
  FirebaseApp productionApp
  
  def init() {
    
    String dirPath = System.getProperty('user.home') + '/.firebase'
    String fileName = 'papx-ws-prod-firebase-sdk.json'
    File file = new File(dirPath, fileName)

    FileInputStream serviceAccount = new FileInputStream(file);
    log.debug('Inicializando Firebase services para PapelWS (PRODUCCION)' )

    FirebaseOptions options = FirebaseOptions.builder()
    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
    .build()

    this.productionApp = FirebaseApp.initializeApp(options, 'productionApp')
  }

  Firestore getFirestore() {
    return FirestoreClient.getFirestore(getApp())
  }

  def getApp() {
    if(!this.productionApp) {
      this.init()
    }
    return this.productionApp
  }
  
  void close() {
    if(this.productionApp) {
      String appName = this.productionApp.name
      this.productionApp.delete()
      this.productionApp = null
      log.debug('Papel firebase ws  {} disconected', appName)
    }
  }
}
