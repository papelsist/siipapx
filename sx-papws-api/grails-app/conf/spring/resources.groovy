import org.springframework.web.servlet.i18n.FixedLocaleResolver

// Place your Spring DSL code here
beans = {
    localeResolver(FixedLocaleResolver, Locale.US){
        defaultLocale = new Locale('es', 'MX')
        Locale.setDefault(defaultLocale)
    }
}
