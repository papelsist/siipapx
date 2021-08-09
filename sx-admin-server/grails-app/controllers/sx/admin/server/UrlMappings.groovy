package sx.admin.server

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        // Catalogos principales
        "/api/sucursales"(resources: "sucursal")
        "/api/sucursales/otrosAlmacenes"(controller: "sucursal", action: 'otrosAlmacenes', method: 'GET')
        "/api/lineas"(resources: "linea")
        "/api/marcas"(resources: "marca")
        "/api/clases"(resources: "clase")
        "/api/productos"(resources: "producto")
        "/api/proveedores"(resources: "proveedor"){
            "/productos"(resources:'proveedorProducto')
        }
        "/api/clientes"(resources: "cliente"){
            "/credito"(resources: 'clienteCredito')
            "/socios"(resources: 'socio')
        }
        "/api/clientes/actualizarCfdiMail/$id"(controller: "cliente", action: 'actualizarCfdiMail', method: 'PUT')
        "/api/clientes/$id/facturas"(controller: 'cliente', action: 'facturas', method: 'GET')
        "/api/clientes/$id/cxc"(controller: 'cliente', action: 'cxc', method: 'GET')
        "/api/clientes/$id/cobros"(controller: 'cliente', action: 'cobros', method: 'GET')
        "/api/clientes/$id/notas"(controller: 'cliente', action: 'notas', method: 'GET')
        // "/api/clientes/$id/socios"(controller: 'cliente', action: 'socios', method: 'GET')
        /*"/api/clientes"(resources: "cliente"){
            
        }
        */

        "/api/clientes/estadoDeCuenta"(controller: "cliente", action: 'estadoDeCuenta', method: 'GET')

        // SAT
        "/api/sat/bancos"(resources: "SatBanco")
        "/api/sat/cuentas"(resources:"SatCuenta")

        // Tesoreria
        "/api/tesoreria/bancos"(resources: "banco")
        "/api/tesoreria/cuentas"(resources: "cuentaDeBanco")
        "/api/tesoreria/fichas"(resources: "ficha"){
            "/cheques"( controller: 'ficha', action: 'cheques')
            "/ingreso"( controller: 'ficha', action: 'ingreso')
        }
        "/api/tesoreria/fichas/generar"(controller: "ficha", action: 'generar', method: 'GET')
        "/api/tesoreria/fichas/reporteDeRelacionDeFichas"(controller: "ficha", action: 'reporteDeRelacionDeFichas', method: 'GET')

        //Comprobantes fiscales de proveedores CFDI's
        "/api/cfdis"(resources: "cfdi")
        "/api/cfdis/mostrarXml/$id?"(controller:"cfdi", action:"mostrarXml")
        "/api/cfdis/descargarXml/$id?"(controller:"cfdi", action:"descargarXml", method: 'GET')
        "/api/cfdis/print/$id"(controller: "cfdi", action: 'print', method: 'GET')
        "/api/cfdis/enviarEmail/$id?"(controller:"cfdi", action:"enviarEmail")
        "/api/cfdis/envioBatch"(controller: "cfdi", action: 'envioBatch', method: 'PUT')
        "/api/cfdis/envioBatchNormal"(controller: "cfdi", action: 'envioBatchNormal', method: 'PUT')


        // Compras
        "/api/compras"(resources: "compra")
        "/api/compras/print/$ID"(controller: 'compra', action: 'print', method: 'GET')

        // Ventas
        "/api/ventas"(resources:"venta")
        "/api/ventas/ventasAcumuladas"(controller: 'ventaCredito', action: 'ventasAcumuladas', method: 'GET')
        "/api/ventas/ventaPorFacturista"(controller: 'ventaCredito', action: 'ventaPorFacturista', method: 'GET')
        "/api/ventas/ventaPorCliente"(controller: 'ventaCredito', action: 'ventaPorCliente', method: 'GET')

        "/api/tesoreria/solicitudes"(resources:"solicitudDeDeposito")
        "/api/tesoreria/solicitudes/pendientes"( controller: 'solicitudDeDeposito', action: 'pendientes')
        "/api/tesoreria/solicitudes/autorizadas"( controller: 'solicitudDeDeposito', action: 'autorizadas')
        "/api/tesoreria/solicitudes/transito"( controller: 'solicitudDeDeposito', action: 'transito')
        "/api/tesoreria/solicitudes/canceladas"( controller: 'solicitudDeDeposito', action: 'canceladas')
        "/api/tesoreria/solicitudes/autorizar/$id"( controller: 'solicitudDeDeposito', action: 'autorizar')
        "/api/tesoreria/solicitudes/posponer/$id"( controller: 'solicitudDeDeposito', action: 'posponer')
        "/api/tesoreria/solicitudes/rechazar/$id"( controller: 'solicitudDeDeposito', action: 'rechazar')
        "/api/tesoreria/solicitudes/cancelar/$id"( controller: 'solicitudDeDeposito', action: 'cancelar')
        "/api/tesoreria/solicitudes/buscarDuplicada/$id"( controller: 'solicitudDeDeposito', action: 'buscarDuplicada')
        "/api/tesoreria/solicitudes/buscarPosibleDuplicadaCallcenter"( controller: 'solicitudDeDeposito', action: 'buscarPosibleDuplicadaCallcenter')
        "/api/tesoreria/solicitudes/ingreso/$id"( controller: 'solicitudDeDeposito', action: 'ingreso', method: 'PUT')
        "/api/tesoreria/solicitudes/cobranzaContado"(controller: 'solicitudDeDeposito', action: 'cobranzaContado', method: 'GET')
        "/api/tesoreria/solicitudes/cobranzaCod"(controller: 'solicitudDeDeposito', action: 'cobranzaCod',  method: 'GET')
        "/api/tesoreria/solicitudes/disponibles"(controller: 'solicitudDeDeposito', action: 'disponibles',  method: 'GET')
        "/api/tesoreria/solicitudes/ventasDiarias"(controller: 'solicitudDeDeposito', action: 'ventasDiarias',  method: 'GET')

        "/api/tesoreria/cortesTarjeta"(resources: 'corteDeTarjeta')
        "/api/tesoreria/cortesTarjeta/pendientes"( controller: 'corteDeTarjeta', action: 'pendientes')
        "/api/tesoreria/cortesTarjeta/generarCortes"( controller: 'corteDeTarjeta', action: 'generarCortes', method: 'POST')
        "/api/tesoreria/cortesTarjeta/ajustarCobro"( controller: 'corteDeTarjeta', action: 'ajustarCobro', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/aplicar/$id"( controller: 'corteDeTarjeta', action: 'aplicar', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/cancelarAplicacion/$id"( controller: 'corteDeTarjeta', action: 'cancelarAplicacion', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/reporteDeComisionesTarjeta"(controller: 'corteDeTarjeta', action: 'reporteDeComisionesTarjeta', method: 'GET')

        "/api/tesoreria/movimientosDeTesoreria"(resources: 'movimientoDeTesoreria')

        // CXC
        "/api/cxc/cobro"(resources: "cobro") {
            "aplicaciones"(resources: 'aplicacionDeCobro', excludes:['create', 'save', 'edit','patch'])
        }
        "/api/cxc/cobro/cobrosMonetarios"(controller: "cobro", action: 'cobrosMonetarios', method: 'GET')
        "/api/cxc/cobro/disponibles"(controller: "cobro", action: 'disponibles', method: 'GET')
        "/api/cxc/cobro/aplicar/$id"(controller: "cobro", action: 'aplicar', method: 'PUT')
        "/api/cxc/cobro/eliminarAplicacion/$id"(controller: "cobro", action: 'eliminarAplicacion', method: 'PUT')
        "/api/cxc/cobro/saldar/$id"(controller: "cobro", action: 'saldar', method: 'PUT')
        "/api/cxc/cobro/registrarChequeDevuelto/$id"(controller: "cobro", action: 'registrarChequeDevuelto', method: 'PUT')
        "/api/cxc/cobro/reporteDeCobranza"(controller: "cobro", action: 'reporteDeCobranza', method: 'GET')
        "/api/cxc/cobro/reporteDeRelacionDePagos"(controller: "cobro", action: 'reporteDeRelacionDePagos', method: 'GET')
        "/api/cxc/cobro/timbrar/$id"(controller: 'cobro', action: 'timbrar', method: 'PUT')
        "/api/cxc/cobro/timbradoBatch"(controller: 'cobro', action: 'timbradoBatch', method: 'PUT')
        "/api/cxc/cobro/reporteDeRecibosPendientes"(controller: "cobro", action: 'reporteDeRecibosPendientes', method: 'GET')


        "/api/cxc/cobro/search"(controller: 'cobro', action: 'search', method: 'GET')
        "/api/cxc/cobro/imprimirRecibo/$id"(controller: 'cobro', action: 'imprimirRecibo', method: 'GET')

        "/api/cobradores"(resources:"cobrador")
        "/api/vendedores"(resources:"vendedor")
        "/api/cxc/despachos"(resources:"despachoDeCobranza")

        "/api/cxc/notas"(resources: "notaDeCredito")
        "/api/cxc/notas/buscarRmd"(controller: "notaDeCredito", action: 'buscarRmd', method: 'GET')
        "/api/cxc/notas/buscarFacturasPendientes"(controller: "notaDeCredito", action: 'buscarFacturasPendientes', method: 'GET')
        "/api/cxc/notas/generarConRmd/$id"(controller: 'notaDeCredito', action: 'generarConRmd', method: 'POST')
        "/api/cxc/notas/timbrar/$id"(controller: 'notaDeCredito', action: 'timbrar', method: 'POST')
        "/api/cxc/notas/aplicar/$id"(controller: 'notaDeCredito', action: 'aplicar', method: 'POST')
        "/api/cxc/notas/print/$id"(controller: 'notaDeCredito', action: 'print', method: 'GET')
        "/api/cxc/notas/reporteDeNotasDeCredito"(controller: 'notaDeCredito', action: 'reporteDeNotasDeCredito', method: 'GET')

        "/api/cxc/notasDeCargo"(resources: "notaDeCargo")
        "/api/cxc/notasDeCargo/timbrar/$id"(controller: 'notaDeCargo', action: 'timbrar', method: 'POST')
        "/api/cxc/notasDeCargo/print/$id"(controller: 'notaDeCargo', action: 'print', method: 'GET')
        "/api/cxc/notasDeCargo/reporteDeNotasDeCargo"(controller: 'notaDeCargo', action: 'reporteDeNotasDeCargo', method: 'GET')


        "/api/cuentasPorCobrar"(resources: 'cuentaPorCobrar')
        "/api/cuentasPorCobrar/search"(controller:'cuentaPorCobrar', action: 'search', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad"(controller: 'cuentaPorCobrar', action: 'antiguedad', method: 'GET')
        /**  Reportes Antigueadd y CXC **/
        "/api/cuentasPorCobrar/antiguedad/print"(controller: 'cuentaPorCobrar', action: 'printAntiguedad', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/reporteDeCobranzaCOD"(controller: 'cuentaPorCobrar', action: 'reporteDeCobranzaCOD', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/antiguedadPorCliente"(controller: 'cuentaPorCobrar', action: 'antiguedadPorCliente', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/clientesSuspendidosCre"(controller: 'cuentaPorCobrar', action: 'clientesSuspendidosCre', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/facturasConNotaDevolucion"(controller: 'cuentaPorCobrar', action: 'facturasConNotaDevolucion', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/reporteExceptionesDescuentos"(controller: 'cuentaPorCobrar', action: 'reporteExceptionesDescuentos', method: 'GET')
        "/api/cuentasPorCobrar/generarPagare"(controller: 'cuentaPorCobrar', action: 'generarPagare', method: 'GET')


        "/api/cuentasPorCobrar/pendientes/$id"(controller: 'cuentaPorCobrar', action: 'pendientes', method: 'GET')
        "/api/cuentasPorCobrar/saldar/$id"(controller: 'cuentaPorCobrar', action: 'saldar', method: 'PUT')

        "/api/cxc/ventaCredito"(resources: 'ventaCredito')
        "/api/cxc/ventaCredito/pendientes"(controller: 'ventaCredito', action: 'pendientes', method: 'GET')
        "/api/cxc/ventaCredito/generar"(controller: 'ventaCredito', action: 'generar', method: 'POST')
        "/api/cxc/ventaCredito/recalcular"(controller: 'ventaCredito', action: 'recalcular', method: 'POST')
        "/api/cxc/ventaCredito/batchUpdate"(controller: 'ventaCredito', action: 'batchUpdate', method: 'POST')
        "/api/cxc/ventaCredito/registrarRecepcionCxC"(controller: 'ventaCredito', action: 'registrarRecepcionCxC', method: 'PUT')
        "/api/cxc/ventaCredito/cancelarRecepcionCxC"(controller: 'ventaCredito', action: 'cancelarRecepcionCxC', method: 'PUT')
        "/api/cxc/ventaCredito/registrarRvisada"(controller: 'ventaCredito', action: 'registrarRvisada', method: 'PUT')
        "/api/cxc/ventaCredito/print"(controller: 'ventaCredito', action: 'print', method: 'GET')

        // Cheques devueltos
        "/api/cxc/cheques"(resources: "chequeDevuelto")
        "/api/cxc/cheques/reporteDeChequesDevueltos"(controller: "chequeDevuelto", action:'reporteDeChequesDevueltos', method: 'GET')
        "/api/cxc/cheques/estadoDeCuentaGeneralChe"(controller: "chequeDevuelto", action:'estadoDeCuentaGeneralChe', method: 'GET')
        "/api/cxc/cheques/estadoDeCuentaDetChe"(controller: "chequeDevuelto", action:'estadoDeCuentaDetChe', method: 'GET')
        "/api/cxc/cheques/saldosPorAbogado"(controller: "chequeDevuelto", action:'saldosPorAbogado', method: 'GET')


        // Comisiones
        "/api/cxc/comisiones"(resources: 'comision')
        "/api/cxc/comisiones/generarComisiones"(controller: 'comision', action: 'generarComisiones', method: 'GET')
        "/api/cxc/comisiones/reporteDeComisiones"(controller: 'comision', action: 'reporteDeComisiones', method: 'GET')


        //Existencias
        "/api/existencias"(resources: "existencia")
        "/api/existencias/$producto/$year/$month"(controller: 'existencia', action: 'buscarExistencias')

        //Bonificaciones mejores clientes
        "/api/crm/mejoresClientes"(resources: "bonificacionMC"){
            "/aplicaciones"(resources: 'bonificacionMCAplicacion', excludes:['create', 'edit','patch'])
        }
        "/api/bonificacionesMC/$ejercicio/$mes"(controller: 'bonificacionMC', action: 'list')
        "/api/bonificacionesMC/generar/$ejercicio/$mes"(controller: 'bonificacionMC', action: 'generar')
        "/api/bonificacionesMC/autorizar/$id"(controller: 'bonificacionMC', action: 'autorizar')
        "/api/bonificacionesMC/autorizarBatch/$ejercicio/$mes"(controller: 'bonificacionMC', action: 'autorizarBatch')
        "/api/bonificacionesMC/suspender/$id"(controller: 'bonificacionMC', action: 'suspender')
        "/api/bonificacionesMC/print"(controller: 'bonificacionMC', action: 'print', method: 'GET')

        "/api/users/buscarPorNip"( controller:'auth', action: 'buscarPorNip', method: 'GET')


        "/api/cxc/juridico"(resources: "Juridico")
        "/api/cxc/juridico/mandarFacturas"(controller: 'juridico', action: 'mandarFacturas', method: 'PUT')

        // Mejoras API MonoRepo
        "/api/devoluciones"(resources: 'devolucionDeVenta', excludes: ['create', 'edit', 'patch'])

        // Security
        "/api/security/users"(resources: "user")
        "/api/security/roles"(resources: "role")
        // "/api/security/users/findByNip"( controller:'user', action: 'findByNip', method: 'GET')


        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
