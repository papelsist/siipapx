package pos.server

import grails.util.Environment
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.cfdi.Cfdi
import sx.cfdi.CfdiService
import sx.core.AppConfig
import sx.core.Cliente
import sx.core.ComunicacionEmpresa
import sx.core.Venta

class EnvioDeCfdisJob {

    CfdiService cfdiService

    static triggers = {
        // simple repeatInterval: 5000l // execute job once in 5 seconds
        simple name:'envioDeCfdi', startDelay: 10000, repeatInterval: 120000
    }

    def execute() {
        if (Environment.current == Environment.PRODUCTION) {
            AppConfig config = AppConfig.first()
            if(config.envioDeCorreosActivo) {
                doEnviar()
            }
        }

    }

    private doEnviar(){
        Date dia = new Date()
        def cfdis = buscarCfdisPendientes(dia)
        cfdis.each {
            Cfdi cfdi = Cfdi.get(it)
            Venta venta = Venta.where{cuentaPorCobrar.cfdi == cfdi}.find()
            if (venta) {
                log.debug('Enviando cfdi venta: {}', venta.statusInfo())
                // Credito
                if (venta.tipo == 'CRE'){
                    cfdiService.enviarFacturaEmail(cfdi, venta, venta.cliente.getCfdiMail())
                } else {
                    Cliente cliente = venta.cliente
                    if (cliente.clave != '1'){
                        ComunicacionEmpresa medio = cliente.medios.find{ it.tipo == 'MAIL' && it.cfdi}
                        if (medio && medio.validado) {
                            String mail = medio.descripcion
                            cfdiService.enviarFacturaEmail(cfdi, venta, mail)
                        }
                    }
                }
            }
        }
    }

    def buscarCfdisPendientes(Date dia) {
        def cfdis = Cfdi.executeQuery("select c.id from Cfdi c where date(c.fecha) = ? " +
                "and c.cancelado = false and enviado = null", [dia])
        return cfdis

    }
}
