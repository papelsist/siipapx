package sx.pos.server

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/api/config"(resource: "appConfig")

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
        "/api/clientes"(resources: "cliente")
        "/api/clientes/actualizarCfdiMail/$id"(controller: "cliente", action: 'actualizarCfdiMail', method: 'PUT')
        "/api/clientes/actualizarTelefono/$id"(controller: "cliente", action: 'actualizarTelefono', method: 'PUT')
        "/api/clientes/validarRfc"(controller: "cliente", action: 'validarRfc', method: 'GET')

        // SAT
        "/api/sat/bancos"(resources: "SatBanco")
        "/api/sat/cuentas"(resources:"SatCuenta")

        // Tesoreria
        "/api/tesoreria/bancos"(resources: "banco")
        "/api/tesoreria/cuentas"(resources: "cuentaDeBanco")
        "/api/tesoreria/fichas"(resources: "ficha")

        //Comprobantes fiscales de proveedores CFDI's
        "/api/cfdis"(resources: "cfdi")
        "/api/cfdis/mostrarXml/$id?"(controller:"cfdi", action:"mostrarXml")
        "/api/cfdis/print/$id"(controller: "cfdi", action: 'print', method: 'GET')
        "/api/cfdis/printRemision/$id"(controller: "cfdi", action: 'printRemision', method: 'GET')
        "/api/cfdis/enviarFacturaEmail"(controller: "cfdi", action: 'enviarFacturaEmail', method: 'PUT')
        "/api/cfdis/envioBatch"(controller: "cfdi", action: 'envioBatch', method: 'PUT')

        // Compras
        "/api/compras"(resources: "compra"){
            "/partidas"(resources:"compraDet")
        }
        "/api/compras/print/$ID"(controller: 'compra', action: 'print', method: 'GET')
        "/api/listaDePreciosPorProveedor"(resources: "listaDePreciosPorProveedor")
        "/api/compras/cerrar/$id"(controller: 'compra', action: 'cerrar', method: 'PUT')
        "/api/compras/depurar"(controller: 'compra', action: 'depurar', method: 'PUT')

        "/api/compras/recepciones"(resources: "recepcionDeCompra") {
            "/partidas"(resource: "recepcionDeCompraDet")
        }
        "/api/compras/recepciones/buscarCompra"(controller: 'recepcionDeCompra', action: 'buscarCompra', method: 'GET')
        "/api/compras/recibir/$id"(controller: 'recepcionDeCompra', action: 'recibir', method: 'PUT')
        "/api/compras/recepciones/print/$id"(controller: 'recepcionDeCompra', action: 'print', method: 'GET')
        "/api/compras/devolucionCompra"(resources: "devolucionDeCompra")

        "/api/compras/recepciones/recepcionDeMercancia"(controller: "recepcionDeCompra", action: 'recepcionDeMercancia', method: 'GET')
        "/api/compras/alcance"(controller: "alcance", action: 'generar', method: 'GET')

        /// Alcances
        "/api/alcances/list"(controller: 'alcances', action: 'list')
        "/api/alcances/generar"(controller: 'alcances', action: 'generar', method: 'POST')
        "/api/alcances/generarOrden"(controller: 'alcances', action: 'generarOrden', method: 'POST')
        "/api/alcances/actualizarMeses"(controller: 'alcances', action: 'actualizarMeses', method: 'PUT')
        "/api/alcances/print"(controller: 'alcances', action: 'print', method: 'GET')

        // Ventas ////////////////////////////////

        // Pedidos
        "/api/pedidos"(resources:"pedido")
        "/api/pedidos/print/$id"(controller: "pedido", action: 'print', method: 'GET')

        // Anticipos
        "/api/cxc/anticipos"(resources: 'anticipo')
        "/api/cxc/anticipos/print"(controller: "anticipo", action: 'print', method: 'GET')

        "/api/ventas"(resources:"venta")
        "/api/ventas/pendientes/$id"( controller: 'venta', action: 'pendientes')
        "/api/ventas/facturados/$id"( controller: 'venta', action: 'facturados')
        "/api/ventas/findManiobra"( controller: 'venta', action: 'findManiobra')
        "/api/ventas/mandarFacturar/$id"( controller: 'venta', action: 'mandarFacturar')
        "/api/ventas/mandarFacturarConAutorizacion"( controller: 'venta', action: 'mandarFacturarConAutorizacion', method: 'POST')
        "/api/ventas/regresaraPendiente/$id"( controller: 'venta', action: 'regresaraPendiente', method: 'PUT')
        "/api/ventas/registrarPuesto/$id"( controller: 'venta', action: 'registrarPuesto', method: 'PUT')
        "/api/ventas/quitarPuesto/$id"( controller: 'venta', action: 'quitarPuesto', method: 'PUT')
        
        "/api/ventas/asignarEnvio/$id"( controller: 'venta', action: 'asignarEnvio', method: 'PUT')
        "/api/ventas/cancelarEnvio/$id"( controller: 'venta', action: 'cancelarEnvio', method: 'PUT')
        "/api/ventas/generarValeAutomatico/$id"( controller: 'venta', action: 'generarValeAutomatico')
        "/api/ventas/facturar/$id"( controller: 'venta', action: 'facturar')
        "/api/ventas/cobradas/$id"( controller: 'venta', action: 'cobradas')
        "/api/ventas/timbrar/$id"( controller: 'venta', action: 'timbrar')
        "/api/ventas/cancelar/$id"( controller: 'venta', action: 'cancelar')
        "/api/ventas/print/$id"(controller: "venta", action: 'print', method: 'GET')
        "/api/ventas/cambioDeCliente/$id"( controller: 'venta', action: 'cambioDeCliente', method: 'PUT')
        "/api/ventas/pedidosPendientes/$id"( controller: 'venta', action: 'pedidosPendientes', method: 'GET')
        "/api/ventas/getPartidas/$id"(controller: 'venta', action: 'buscarPartidas', method: 'GET')
        "/api/ventas/noFacturables"( controller: 'venta', action: 'noFacturables')
        "/api/ventas/regresarCallcenter/$id"( controller: 'venta', action: 'regresarCallcenter', method: 'PUT')



        "/api/preciosPorCliente"(resources: 'preciosPorCliente')
        "/api/preciosPorCliente/buscarPrecio"(controller: 'preciosPorCliente', action: 'buscarPrecio', method: 'GET')
        "/api/preciosPorCliente/preciosPorCliente"(controller: 'preciosPorCliente', action: 'preciosPorCliente', method: 'GET')
        "/api/descuentoPorVolumen"(resources: 'descuentoPorVolumen')

        "/api/socios"(resources:"socio")

        "/api/tesoreria/solicitudes"(resources:"solicitudDeDeposito")
       "/api/tesoreria/solicitudes/buscarDuplicada"( controller: 'solicitudDeDeposito', action: 'buscarDuplicada')
        "/api/tesoreria/solicitudes/pendientes/$id"( controller: 'solicitudDeDeposito', action: 'pendientes')
        "/api/tesoreria/corteCobranza"(resources:"corteCobranza")
        "/api/tesoreria/corteCobranza/cortes"(controller:"corteCobranza", action: 'cortes', method: 'GET')
        "/api/tesoreria/corteCobranza/preparar"(controller:"corteCobranza", action: 'preparar', method: 'GET')
        "/api/tesoreria/corteCobranza/corteChequeInfo"(controller:"corteCobranza", action: 'corteChequeInfo', method: 'GET')
        "/api/tesoreria/fondoFijo"(resources:"fondoFijo")
        "/api/tesoreria/fondoFijo/fondos"(controller:"fondoFijo", action: "fondos", method: "GET")
        "/api/tesoreria/fondoFijo/pendientes"(controller:"fondoFijo", action: "pendientes", method: "GET")
        "/api/tesoreria/fondoFijo/prepararRembolso"(controller:"fondoFijo", action: "prepararRembolso", method: "GET")

        "/api/tesoreria/fondoFijo/solicitarRembolso"(controller:"fondoFijo", action:'solicitarRembolso', method: 'PUT')
        "/api/tesoreria/morralla"(resources:"morralla")
        "/api/tesoreria/reporteDeAarqueoCaja"(controller: 'cobro', action: 'reporteDeAarqueoCaja', method: 'GET')
        "/api/tesoreria/reporteDeFichas"(controller: 'cobro', action: 'reporteDeFichas', method: 'GET')



        // CXC
        "/api/cxc/cobro"(resources: "cobro")
        "/api/cxc/cobro/cobroContado"(controller: 'cobro', action: 'cobroContado')
        "/api/cxc/cobro/cambioDeCheque"(controller: 'cobro', action: 'cambioDeCheque')
        "/api/cxc/cobro/ventasFacturables"(controller: 'cobro', action: 'ventasFacturables', method: 'GET')
        "/api/cxc/cobro/buscarDisponibles/$id"(controller: 'cobro', action: 'buscarDisponibles')
        "/api/cxc/cobro/buscarBonificacionesMC/$id"(controller: 'cobro', action: 'buscarBonificacionesMC')
        "/api/cxc/cobro/generarDisponiblesMC/$id"(controller: 'cobro', action: 'generarDisponiblesMC')

        "/api/cxc/cobro/buscarAnticiposDisponibles"(controller: 'cobro', action: 'buscarAnticiposDisponibles')
        "/api/cxc/cobro/registrarCobroConAnticipo"(controller: 'cobro', action: 'registrarCobroConAnticipo', method: 'POST')
        "/api/cxc/cobro/reporteDeAnticipos"(controller: 'cobro', action: 'reporteDeAnticipos', method: 'GET')

        "/api/notasDeCargo"(resources: "notaDeCargo")
        "/api/cuentasPorCobrar"(resources: 'cuentaPorCobrar')
        "/api/cuentasPorCobrar/buscarVenta/$id"(controller: 'cuentaPorCobrar', action: 'buscarVenta', method: 'GET')
        "/api/cuentasPorCobrar/getPartidas/$id"(controller: 'cuentaPorCobrar', action: 'buscarPartidas', method: 'GET')

        "/api/cuentasPorCobrar/pendientesCod/$id"( controller: 'cuentaPorCobrar', action: 'pendientesCod')
        "/api/cxc/canceladas/$id"(controller: "cuentaPorCobrar", action: 'canceladas', method: 'GET')

        "/api/remoteCredito"(resources: "remoteCredito")
        "/api/remoteCredito/actualizarCredito"(controller: 'remoteCredito', action:'actualizarCredito')

        //Existencias
        "/api/existencias"(resources: "existencia"){
            collection {
                "/sucursal"(controller: 'existencias', action: 'existenciasPorSucursal', method: 'GET')
            }
        }
        "/api/existencias/$producto/$year/$month"(controller: 'existencia', action: 'buscarExistencias')
        "/api/existencias/reporteDeDiscrepancias"(controller: 'existencia', action: 'reporteDeDiscrepancias')
        "/api/existencias/recortePorDetalle"(controller: 'existencia', action: 'recortePorDetalle')


        //Inventario
        "/api/inventario"(resources: "inventario")
        "/api/inventario/movimientos"(resources: "movimientoDeAlmacen")
        "/api/inventario/movimientos/print"(controller: "movimientoDeAlmacen", action: 'print', method: 'GET')
        "/api/inventario/transformaciones"(resources: "transformacion")
        "/api/inventario/transformaciones/print"(controller: "transformacion", action: 'print', method: 'GET')
        "/api/inventario/devoluciones"(resources: "devolucionDeVenta")
        "/api/inventario/devoluciones/buscarVenta"(controller: 'devolucionDeVenta', action: 'buscarVenta', method: 'GET')
        "/api/inventario/devoluciones/print"(controller: "devolucionDeVenta", action: 'print', method: 'GET')
        "/api/inventario/recalcular"(controller: 'existencia', action: 'recalcular', method: 'GET')
        "/api/inventario/puestos"(controller: "movimientoDeAlmacen", action: 'puestos', method: 'GET')


        // Decs
        "/api/inventario/decs"(resources: "devolucionDeCompra")
        "/api/inventario/decs/buscarCom"(controller: 'devolucionDeCompra', action: 'buscarCom', method: 'GET')
        // "/api/inventario/decs/print/$id"(controller: "devolucionDeCompra", action: 'print', method: 'GET')
        "/api/inventario/decs/print"(controller: 'devolucionDeCompra', action: 'print', method: 'GET')
        // "/api/inventario/decs/print/$id"(controller: 'devolucionDeCompra', action: 'print', methid: 'GET')

        // Sols
        "/api/inventario/sols"(resources: "solicitudDeTraslado")
        "/api/inventario/sols/print"(controller: "solicitudDeTraslado", action: 'print', method: 'GET')
        "/api/inventario/sols/atender/$id"(controller: "solicitudDeTraslado", action: 'atender', method: 'PUT')

        // Traslados
        "/api/inventario/traslados"(resources: "traslado")
        "/api/inventario/traslados/print"(controller: "traslado", action: 'print', method: 'GET')
        "/api/inventario/traslados/printCfdi"(controller: "traslado", action: 'printCfdi', method: 'GET')
        "/api/inventario/traslados/salida/$id"(controller: "traslado", action: 'salida', method: 'PUT')
        "/api/inventario/traslados/timbrar/$id"(controller: "traslado", action: 'timbrar', method: 'PUT')
        "/api/inventario/traslados/entrada/$id"(controller: "traslado", action: 'entrada', method: 'PUT')
        "/api/inventario/traslados/reportes/relaciontps"(controller: "traslado", action: 'relacionTps', method: 'GET')
        "/api/inventario/traslados/reportes/relaciontpe"(controller: "traslado", action: 'relacionTpe', method: 'GET')
        "/api/inventario/traslados/reportes/valesxrecibir"(controller: "traslado", action: 'valesPendienteRecibir', method: 'GET')
        "/api/inventario/traslados/reportes/solesxatender"(controller: "traslado", action: 'solPendientesAtender', method: 'GET')

        // Kardex
        "/api/inventario/kardex"(controller: "inventario", action: "kardex" )
        "/api/inventario/printKardex"(controller: "inventario", action: "printKardex", method: 'GET' )
        "/api/inventario/saveInventario"(controller: "inventario", action: "saveInventario" , method: 'POST')

        // Sectores
        "/api/inventario/sectores"(resources: "sector")
        "/api/inventario/sectores/print/$id"(controller: "sector", action: 'print', method: 'GET')
        "/api/inventario/sectores/productosSinSector"(controller: "sector", action: 'productosSinSector', method: 'GET')
        "/api/inventario/sectores/recorridosPorLinea"(controller: "sector", action: 'recorridosPorLinea', method: 'GET')

        // Conteos
        "/api/inventario/conteos"(resources: "conteo")
        "/api/inventario/conteos/generarConteo"(controller: "conteo", action: 'generarConteo', method: 'POST')
        "/api/inventario/conteos/generarExistencias"(controller: "conteo", action: ' generarExistencias', method: 'GET')
        "/api/inventario/conteos/limpiarExistencias"(controller: "conteo", action: ' limpiarExistencias', method: 'GET')
        "/api/inventario/conteos/print/$id"(controller: "conteo", action: 'print', method: 'GET')
        "/api/inventario/conteos/imprimirSectores"(controller: "conteo", action: 'imprimirSectores', method: 'GET')
        "/api/inventario/conteos/noCapturados"(controller: "conteo", action: 'reporteNoCapturados', method: 'GET')
        "/api/inventario/conteos/validacion"(controller: "conteo", action: 'reporteValidacion', method: 'GET')
        "/api/inventario/conteos/diferencias"(controller: "conteo", action: 'reporteDiferencias', method: 'GET')
        "/api/inventario/conteos/fijarConteo"(controller: "conteo", action: 'fijarConteo', method: 'POST')
        "/api/inventario/conteos/ajustarConteo"(controller: "conteo", action: 'ajustePorConteo', method: 'POST')
        "/api/inventario/conteos/cargarSector"(controller: "conteo", action: 'cargarSector', method: 'POST')
        "/api/inventario/conteos/generarExistenciaParcial"(controller: "conteo", action: 'generarExistenciaParcial', method: 'GET')

        // Embarques
        "/api/embarques/facturistas"(resources: 'facturistaDeEmbarque')
        "/api/embarques/transportes"(resources: 'transporte')
        "/api/embarques/choferes"(resources: "chofer")
        "/api/embarques/embarques"(resources: "embarque")
        "/api/embarques/embarques/buscarDocumento"(controller: 'embarque', action: 'buscarDocumento', method: 'GET')
        "/api/embarques/embarques/registrarSalida/$id"(controller: 'embarque', action: 'registrarSalida', method: 'PUT')
        "/api/embarques/embarques/registrarRegreso/$id"(controller: 'embarque', action: 'registrarRegreso', method: 'PUT')
        "/api/embarques/embarques/print"(controller: "embarque", action: 'print', method: 'GET')
        "/api/embarques/embarques/reporteDeEntregasPorChofer"(controller: "embarque", action: 'reporteDeEntregasPorChofer', method: 'GET')
        "/api/embarques/embarques/documentosEnTransito"(controller: "embarque", action: 'documentosEnTransito', method: 'GET')
        "/api/embarques/embarques/enviosPendientes"(controller: "embarque", action: 'enviosPendientes', method: 'GET')
        "/api/embarques/embarques/buscarVenta"(controller: 'embarque', action: 'buscarVenta', method: 'GET')
        "/api/embarques/embarques/buscarPartidasDeVenta"(controller: 'embarque', action: 'buscarPartidasDeVenta', method: 'GET')
        "/api/embarques/embarques/buscarTrasladosPendientes"(controller: 'embarque', action: 'buscarTrasladosPendientes', method: 'GET')
        "/api/embarques/embarques/buscarDevolucionesPendientes"(controller: 'embarque', action: 'buscarDevolucionesPendientes', method: 'GET')
        "/api/embarques/embarques/asignarFacturas"(controller: 'embarque', action: 'asignarFacturas', method: 'PUT')
        "/api/embarques/envios"(resources: 'envio')
        "/api/embarques/embarques/reporteFacturaEnvio"(controller: "embarque", action: 'reporteFacturaEnvio', method: 'GET')
        "/api/embarques/codigos"(controller:"envio", action: " buscarCodigoPostal")

        "/api/report"(controller: 'reporte', action: 'run', method: 'GET')
        "/api/report/ventasDiarias"(controller: 'ventas', action: 'ventasDiarias', method: 'GET')
        "/api/report/cobranzaCod"(controller: 'ventas', action: 'cobranzaCod', method: 'GET')
        "/api/report/cobranzaEfectivo"(controller: 'ventas', action: 'cobranzaEfectivo', method: 'GET')
        "/api/report/cobranzaContado"(controller: 'ventas', action: 'cobranzaContado', method: 'GET')
        "/api/report/facturasCanceladas"(controller: 'ventas', action: 'facturasCanceladas', method: 'GET')
        "/api/report/aplicacionSaldos"(controller: 'ventas', action: 'aplicacionDeSaldos', method: 'GET')
        "/api/report/disponiblesSucursal"(controller: 'ventas', action: 'disponiblesSucursal', method: 'GET')
        "/api/report/facturasPendientesCod"(controller: 'ventas', action: 'facturasPendientesCod', method: 'GET')
        "/api/report/facturasPendientesCodEmbarques"(controller: 'ventas', action: 'facturasPendientesCodEmbarques', method: 'GET')
        "/api/report/ventasDiariasCheques"(controller: 'ventas', action: 'ventasDiariasCheques', method: 'GET')
        "/api/report/clientesNuevos"(controller: 'ventas', action: 'clientesNuevos', method: 'GET')


        // Cajas
         "/api/cajas/cotizaciones/cotizacion"(resources: "cotizacionCaja")
         "/api/cajas/cotizaciones"(controller: "cotizacionCaja", action: 'list', method: 'GET')
         "/api/cajas/cotizaciones/save"(controller: "cotizacionCaja", action: 'save', method: 'POST')
         "/api/cajas/cotizaciones/cerrar/$id"(controller: "cotizacionCaja", action: 'cerrar', method: 'PUT')
         "/api/cajas/cotizaciones/print/$id"(controller: "cotizacionCaja", action: 'print', method: 'GET')

        // logistica

        "/api/logistica/soporte"(controller: "solicitudCambio", action: 'list', method: 'GET')
         "/api/logistica/soporte/tipos"(controller: "solicitudCambio", action: 'tipos', method: 'GET')
        "/api/logistica/soporte/salvar"(controller: "solicitudCambio", action: 'salvar', method: 'POST')

        "/api/complementosIne"(resources:"complementoIne")
        "/api/complementosIne/pendientes"(controller: "complementoIne", action: 'pendientes', method: 'GET')

        // Security
        "/api/security/users"(resources: "user")
        "/api/security/roles"(resources: "role")
        "/api/security/users/findByNip"( controller:'user', action: 'findByNip', method: 'GET')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
