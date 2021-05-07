import com.luxsoft.CustomAuditLogListener
import com.luxsoft.cfdix.v33.CfdiCadenaBuilder33
import com.luxsoft.cfdix.v33.CfdiFacturaBuilder
import com.luxsoft.cfdix.v33.CfdiSellador33
import com.luxsoft.cfdix.v33.TrasladoBuilder
import org.springframework.web.servlet.i18n.FixedLocaleResolver
import sx.core.ExistenciaListener


// Place your Spring DSL code here
beans = {
    customAuditLogListener(CustomAuditLogListener) {
        dataSource = ref('dataSource')
    }

    cfdiCadenaBuilder(CfdiCadenaBuilder33){}

    cfdiSellador(CfdiSellador33){
        cadenaBuilder = ref('cfdiCadenaBuilder')
    }

    cfdiFacturaBuilder(CfdiFacturaBuilder) {
        sellador = ref('cfdiSellador')
    }

    trasladoBuilder(TrasladoBuilder) {
        sellador = ref('cfdiSellador')
    }

    localeResolver(FixedLocaleResolver, Locale.US){
        defaultLocale = new Locale('es', 'MX')
        Locale.setDefault(defaultLocale)
    }

    existenciaListener(ExistenciaListener) {
        cloudService = ref('cloudService')
        lxExistenciaService = ref('lxExistenciaService')
    }
}
