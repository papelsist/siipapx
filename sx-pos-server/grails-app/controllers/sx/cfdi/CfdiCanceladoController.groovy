package sx.cfdi

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.luxsoft.cfdix.v33.V33PdfGenerator
import grails.rest.RestfulController
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class CfdiCanceladoController extends RestfulController {

    CfdiTimbradoService cfdiTimbradoService

    ReportService reportService

    static responseFormats = ['json']

    CfdiCanceladoController(){
        super(CfdiCancelado)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 500
        def query = CfdiCancelado.where {}

        def list = query.list(params)
        return list
    }

    def mostrarXml(CfdiCancelado cfdiCancelado){
        if(cfdiCancelado == null ){
            notFound()
            return
        }
        String aka=new String(cfdiCancelado.getAka())
        render(text: aka, contentType: "text/xml", encoding: "UTF-8")
    }

    def print( CfdiCancelado cfdiCancelado) {
        def pdf = this.reportService.run('CancelacionDeCfdi', params)
        def fileName = "CancelacionDeCfdi.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

}