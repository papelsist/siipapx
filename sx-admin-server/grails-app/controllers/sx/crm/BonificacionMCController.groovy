package sx.crm

import grails.compiler.GrailsCompileStatic
import grails.rest.*
import grails.converters.*
import sx.reports.ReportService

@GrailsCompileStatic
class BonificacionMCController extends RestfulController<BonificacionMC> {
    static responseFormats = ['json']
    BonificacionMCService bonificacionMCService
    ReportService reportService
    BonificacionMCController() {
        super(BonificacionMC)
    }

    def list(Integer ejercicio, Integer mes){
        respond BonificacionMC.where{ejercicio == ejercicio && mes == mes}.list()
    }

    def generar(Integer ejercicio, Integer mes) {
        bonificacionMCService.generar(ejercicio, mes)
        respond BonificacionMC.where{ejercicio == ejercicio && mes == mes}.list()
    }

    def autorizar(BonificacionMC bonificacionMC) {
        respond bonificacionMCService.autorizar(bonificacionMC)
    }

    def suspender(BonificacionMC bonificacionMC) {
        String comentario = params.comentario
        respond bonificacionMCService.suspender(bonificacionMC, comentario)
    }

    def autorizarBatch(Integer ejercicio, Integer mes) {
        List<BonificacionMC> rows = BonificacionMC.where{ejercicio == ejercicio && mes == mes}.list()
        List<BonificacionMC> autorizadas = []
        rows.each {
            BonificacionMC res = bonificacionMCService.autorizar(it)
            autorizadas << res
        }
        respond autorizadas
    }

    def print(Integer ejercicio, Integer mes) {
        Map repParams = [:]
        repParams.EJERCICIO = ejercicio
        repParams.MES = mes
        repParams.MES_NOMBRE = params.mesNombre
        def pdf =  reportService.run('BonificacionesMC.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'BonificacionesMC.pdf')
    }
}
