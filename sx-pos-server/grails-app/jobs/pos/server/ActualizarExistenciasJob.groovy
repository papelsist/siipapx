package pos.server

import grails.util.Environment
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.ExistenciaService
import sx.core.Inventario
import sx.core.Producto

@Slf4j
class ActualizarExistenciasJob {

    ExistenciaService existenciaService

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
        cron name: 'everyTrigger', cronExpression: "0 0 8 ? * SUN"
    }

    def execute() {
        if(Environment.isDevelopmentMode()){
            log.info('Development Environment no se ejecuta la actualizacion de existencias')
            return
        }

        Date hoy = new Date()
        def ejercicio = hoy[Calendar.YEAR]
        def mes = hoy[Calendar.MONTH] + 1
        Set productos = Inventario.executeQuery('select distinct(e.producto.id) ' +
                'from Inventario e where date(e.lastUpdated) = ? ',
                [new Date()])
        log.debug('Actualizando existencia de {} productos', productos.size())
        productos.each { String id ->

            try {
                Producto producto = Producto.get(id)
                def exis = existenciaService.recalcular(producto, ejercicio , mes)
                log.debug("Existencia de {} actualizada: {}", producto.clave, exis.cantidad)
            }catch (Exception ex){
                log.debug( 'Error al actualiza existencia de  {} Ex: {}',
                        id, ExceptionUtils.getRootCauseMessage(ex))
            }

        }
        log.debug("Existencia  actualizada")
    }
}
