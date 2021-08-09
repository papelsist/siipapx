package sx.core

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.inventario.Traslado

@Transactional
class ExistenciaService {

    @Subscriber
    def onFacturar(Venta factura) {
        if( factura.cuentaPorCobrar) {
            actualizarExistenciasPorFactura(factura)
        }
    }

    private actualizarExistenciasPorFactura(Venta venta) {
        Date hoy = new Date()
        int month = hoy[Calendar.MONTH] + 1
        def year = hoy[Calendar.YEAR]
        Existencia.withNewTransaction {
            Venta factura = Venta.get(venta.id)
            log.debug("Actualizando existencias por ${factura.statusInfo()}")
            factura.partidas.each { VentaDet det ->
                Existencia exis = Existencia.where{ producto == det.producto && anio == year && mes== month}.find()
                assert exis, "No existe existencia ${year} - ${month} Para ${det.producto.clave}"
                exis.venta = exis.venta - det.cantidad
                exis.save()
            }
        }
    }

    def actualizarExistencias(Long ejercicio, Long mes, Producto producto) {

        def mesAnterior = mes == 1 ? 12 : mes - 1
        def ejercicioAnterior = mes == 1 ? ejercicio -1 : ejercicio
        def anterior = findExitencia(producto, ejercicioAnterior, mesAnterior)
        def actual = findOrCreateNext(anterior)
        actual.with {
        }
    }

    Existencia findOrCreateNext(Existencia existencia) {
        def year = existencia.mes == 12 ? existencia.anio + 1 : existencia.anio
        def mes = existencia.mes == 12 ? 1 : existencia.mes + 1
        return Existencia.findOrSaveWhere(
                sucursal: getSucursal(),
                producto: existencia.producto,
                ejercicio: year,
                mes: mes);
    }

    @Subscriber
    def onRegistrarEntradaPorTpe(List<Inventario> entradas){
        log.debug('Afectando existencias por entada de TPE ')
        Existencia.withNewTransaction {
            entradas.each {
                int ejercicio = it.fecha[Calendar.YEAR]
                int mes = it.fecha[Calendar.MONTH] + 1
                Existencia e = findExitencia(it.producto, ejercicio, mes)
                if (e) {
                    e.cantidad += it.cantidad
                    // e.traslado += it.cantidad
                }
                e.save()
            }
        }
    }

    @Subscriber
    def onRegistrarSalidaPorTps(List<Inventario> entradas){
        log.debug('Afectando existencias por entada de TPS ')
        Existencia.withNewTransaction {
            entradas.each {
                int ejercicio = it.fecha[Calendar.YEAR]
                int mes = it.fecha[Calendar.MONTH] + 1
                Existencia e = findExitencia(it.producto, ejercicio, mes)
                if (e) {
                    e.cantidad += it.cantidad
                    // e.traslado += it.cantidad
                }
                e.save()
            }
        }
    }

    Existencia findExitencia(Producto producto, Long ejercicio, Long mes) {
        return Existencia.where {
            sucursal == getSucursal() &&
            producto == producto &&
            anio == ejercicio &&
            mes == mes }.find()
    }

    private Sucursal sucursal

    Sucursal getSucursal() {
        if(sucursal == null){
            sucursal = AppConfig.first().sucursal
        }
        return sucursal
    }
}
