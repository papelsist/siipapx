package sx.admin.server

import sx.cxc.RevisionService

class RecalcularRevisionJob {

    RevisionService revisionService

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
        // simple name:'simpleTrigger', startDelay: 10000, repeatInterval: 60000 , repeatCount: 10
        cron name: 'everyTrigger', cronExpression: "0 0 18 ? * MON-SAT"
    }

    def execute() {
        log.debug('Actualizando existencia revision de cuentas por cobrar')
        // revisionService.actualizar()
    }
}
