package sx.integracion

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import sx.cfdi.Cfdi
import sx.core.Sucursal
import sx.cxc.CuentaPorCobrar

@Component
@Slf4j
class CfdisIntegracion implements  Integracion {

    def ajustarNombre(Sucursal sucursal){
        int last =  CuentaPorCobrar.where{sucursal == sucursal && cfdi!= null}.count()
        int page = 20
        int offset = 0
        for(offset; offset< last; ){
            Cfdi.withSession{ session ->
                List rows = CuentaPorCobrar.where{sucursal == sucursal && cfdi!= null}.list(offset:offset, max:page).collect{ it.cfdi}
                rows.each { Cfdi cfdi ->
                    fixName(cfdi)
                }
                session.flush()
            }
            offset+= page;
        }
    }

    def fixName(Cfdi cfdi){
        String urlPath = cfdi.url.path
        int index = cfdi.url.path.lastIndexOf('/') + 1
        String name = StringUtils.substringAfterLast(urlPath, '/')
        cfdi.fileName = name
        cfdi.save()
    }

    def validarExistenciaDeXml(Sucursal sucursal) {
        int last =  CuentaPorCobrar.where{sucursal == sucursal && cfdi!= null}.count()
        int page = 20
        int offset = 0
        for(offset; offset< last; ){
            Cfdi.withSession{ session ->
                List rows = CuentaPorCobrar.where{sucursal == sucursal && cfdi!= null}.list(offset:offset, max:page).collect{ it.cfdi}
                rows.each { Cfdi cfdi ->
                    validarXml(cfdi, sucursal)
                }
                session.flush()
            }
            offset+= page;
        }
    }

    def validarXml(Cfdi cfdi, Sucursal sucursal){
        //String cfdiDir = '/Users/ruben/dumps/cfdis'
        String cfdiDir = '/Volumes/xml'
        Date fecha = cfdi.fecha
        String year = fecha[Calendar.YEAR]
        String mes = fecha[Calendar.MONTH] +1
        String dia = fecha[Calendar.DATE]
        File dir = new File("${cfdiDir}/${sucursal.nombre.toLowerCase()}/${year}/${mes}/${dia}")
        File xml = new File(dir,cfdi.fileName)
        if(!xml.exists()){
            println "NO EXISTE EL XML PARA EL CFDI: ${cfdi.id}"
        }
    }
}
