package sx.cloud.papws

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.cloud.StorageClient
import grails.util.Environment
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils

import javax.annotation.PreDestroy

@Slf4j
class CloudService {

  private FirebaseApp papelws
  String projectId
  String firebaseBucket

  void init() {
    this.projectId = 'papx-ws-prod'
    this.firebaseBucket = 'papx-ws-prod.appspot.com'
    String dirPath = '.'
    String fileName = 'papx-ws-prod-firebase-sdk.json'
    if(Environment.current == Environment.DEVELOPMENT) {
      dirPath = System.getProperty('user.home') + '/.firebase'
      fileName = 'papx-ws-dev-firebase-sdk.json'
      this.projectId = 'papx-ws-dev'
      this.firebaseBucket = 'papx-ws-dev.appspot.com'
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
    return FirestoreClient.getFirestore(this.getApp())
  }


  FirebaseApp getApp() {
    if(this.papelws == null) {
      this.init()
    }
    return this.papelws
  }


  void publishDocument(String objectName, byte[] data, String contentType, Map metaData) {
    if(this.papelws == null) {
      this.init()
    }
    log.debug("Publicando documento: {} Data length: {}", objectName, data.length)

    try {
      String projectId = this.projectId
      String bucketName = this.firebaseBucket

      BlobId blobId = BlobId.of(bucketName, objectName)
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(contentType)
        .setMetadata(metaData)
        .build()

      StorageClient storageClient = StorageClient.getInstance(getApp())
      Bucket bucket = storageClient.bucket(this.firebaseBucket)
      Storage storage = bucket.getStorage()
      storage.create(blobInfo, data)
      log.info('Document {} Uploaded to PAPWS {}', objectName, this.firebaseBucket)
    }catch (Exception ex) {
      log.error('Error subiendo documento ex:' + ExceptionUtils.getRootCauseMessage(ex), ex)
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
