import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter
import ch.qos.logback.core.util.FileSize
import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

def targetDir = BuildSettings.TARGET_DIR
def USER_HOME = System.getProperty("user.home")
def HOME_DIR = Environment.isDevelopmentMode() ? targetDir : '.'
appender('FIREBASE', RollingFileAppender) {
    append = false
    encoder(PatternLayoutEncoder) {
        pattern =
          '%clr(%d{HH:mm}){faint} ' + // Date
            '%clr(%5p) ' + // Log level
            '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
            '%m%n%wex' // Message
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${HOME_DIR}/logs/papws-firebase-%d{yyyy-MM-dd}.log"
        maxHistory = 5
        totalSizeCap = FileSize.valueOf("1GB")
    }
}


if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
if (Environment.isDevelopmentMode()) {
    logger("org.pac4j", OFF, ['STDOUT'], false)
    logger("papws.api", DEBUG, ['STDOUT'], false)
}
if (Environment.current == Environment.PRODUCTION) {
    logger("org.pac4j", OFF, ['STDOUT'], false)
    logger("papws.api", INFO, ['STDOUT', 'FIREBASE'], false)

}  

root(ERROR, ['STDOUT'])


