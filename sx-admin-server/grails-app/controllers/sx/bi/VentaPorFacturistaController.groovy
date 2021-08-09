package sx.bi


import grails.rest.RestfulController
import sx.reports.ReportService

class VentaPorFacturistaController extends RestfulController {
    static responseFormats = ['json']

    ReportService reportService

    VentaPorFacturistaController() {
        super(VentaPorFacturista)
    }

    def runReport() {

    }
}
