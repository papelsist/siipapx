package sx.cxc

import com.luxsoft.utils.Periodo
import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController
import sx.reports.ReportService

@GrailsCompileStatic
class ComisionController extends RestfulController<Comision>{

    static responseFormats = ['json']

    ComisionService comisionService

    ReportService reportService

    ComisionController(){
        super(Comision);
    }

    @Override
    protected List<Comision> listAllResources(Map params) {
        if(params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            return Comision.where{ fechaIni == periodo.fechaInicial && fechaFin == periodo.fechaFinal}.list()
        }
        return super.listAllResources(params)
    }

    def generarComisiones(GenerarComisionesCommand command) {
        List res = comisionService.generarComisionesCobrador(command.tipo, command.fechaInicial, command.fechaFinal)
        respond res

    }

    def reporteDeComisiones(ReporteDeComisionesCommand command) {
        def repParams = [:]
        repParams.FECHA_INI = command.fechaInicial
        repParams.FECHA_FIN = command.fechaFinal
        repParams.TIPO = command.tipo
        repParams.COMISIONISTA = command.comisionista.toString()
        if (command.comisionista == 0) {
            repParams.COMISIONISTA = '%'
        }
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('Comisiones.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Comisiones.pdf')
    }
}

class GenerarComisionesCommand {
    Date fechaInicial
    Date fechaFinal
    String tipo
}

class ReporteDeComisionesCommand {

    Date fechaInicial
    Date fechaFinal
    String tipo
    Integer comisionista = 0

}
