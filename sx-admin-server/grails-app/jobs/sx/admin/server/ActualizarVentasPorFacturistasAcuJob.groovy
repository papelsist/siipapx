package sx.admin.server

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.integracion.VentaPorFacturistaIntegration

@Slf4j
class ActualizarVentasPorFacturistasAcuJob {

    VentaPorFacturistaIntegration ventaPorFacturistaIntegration

    /**
     * cronExpression: "s m h D M W Y"
     *                  | | | | | | `- Year [optional]
     *                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
     *                  | | | | `- Month, 1-12 or JAN-DEC
     *                  | | | `- Day of Month, 1-31, ?
     *                  | | `- Hour, 0-23
     *                  | `- Minute, 0-59
     *                  `- Second, 0-59
     */
    static triggers = {
        cron name: 'everyTrigger', cronExpression: "0 0 5,20 ? * MON-SAT"
    }

    def execute() {
        try {
            ventaPorFacturistaIntegration.actualizarMTD()
        }catch (Exception ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error(msg)
        }
    }
}
