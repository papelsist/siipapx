package sx.inventario

import com.luxsoft.utils.MonedaUtils
import grails.events.annotation.Publisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import sx.core.Folio
import sx.core.Inventario
import sx.core.Producto
import sx.logistica.Chofer

@Transactional
class TrasladoService {

    SpringSecurityService springSecurityService

    @Subscriber
    public atender(Map  map ){
        log.debug('Atendiendo solicitud de traslado: {}', map);
        Traslado.withNewTransaction {
            SolicitudDeTraslado origen = SolicitudDeTraslado.get(map.sol)
            generarTpe(origen)
            generarTps(origen, map.chofer, map.comentario)
            origen.atender = new Date()
            origen.save()
        }
    }

    private generarTpe( SolicitudDeTraslado sol) {
        log.debug('Generando TPE para Sol: {}', sol);
        Traslado traslado = new Traslado()
        traslado.sucursal = sol.sucursalSolicita
        traslado.fecha = new Date()
        traslado.documento = sol.documento
        traslado.tipo = 'TPE'
        traslado.comentario = sol.comentario
        traslado.clasificacionVale = sol.clasificacionVale
        traslado.kilos = 0
        traslado.porInventario = false
        traslado.solicitudDeTraslado = sol
        sol.partidas.findAll{ it.recibido > 0}.each { SolicitudDeTrasladoDet solDet ->
            TrasladoDet det = new TrasladoDet()
            det.producto = solDet.producto
            det.comentario = solDet.comentario
            det.solicitado = solDet.solicitado
            det.cantidad = solDet.recibido
            det.kilos = calcularKilos(det.producto, solDet.recibido.abs())
            traslado.kilos += det.kilos
            det.cortes = solDet.cortes
            det.cortesInstruccion = solDet.cortesInstruccion
            traslado.addToPartidas(det)
        }
        logEntity(traslado)
        traslado.save()
        log.debug('TPE generado: {}', traslado)
        return traslado
    }

    private generarTps(SolicitudDeTraslado sol, Chofer chofer, String comentario){
        assert chofer, 'Se requiere el chofer para atender el traslado'
        assert comentario, 'Se requiere el comentario al atender el traslado'
        log.debug('Generando TPS para Sol:{} Chofer:{} comentario: {}', sol.id, chofer.id, comentario);
        Traslado tps = new Traslado()
        tps.sucursal = sol.sucursalAtiende
        tps.fecha = new Date()
        tps.documento = getFolio()
        tps.comentario = comentario
        tps.chofer = chofer
        tps.tipo = 'TPS'
        tps.clasificacionVale = sol.clasificacionVale
        tps.porInventario = false
        tps.solicitudDeTraslado = sol
        sol.partidas.findAll{ it.recibido > 0}.each { SolicitudDeTrasladoDet solDet ->
            TrasladoDet det = new TrasladoDet()
            det.producto = solDet.producto
            det.comentario = solDet.comentario
            det.solicitado = solDet.solicitado
            det.cantidad = solDet.recibido.abs() * -1
            det.kilos = calcularKilos(det.producto, solDet.recibido.abs())
            tps.kilos += det.kilos
            det.cortes = solDet.cortes
            det.cortesInstruccion = solDet.cortesInstruccion
            tps.addToPartidas(det)
        }

        logEntity(tps)
        tps.save()
        log.debug('TPS generado: {}', tps)
        return tps
    }

    def logEntity(Traslado traslado) {
        def user = getUser()
        if(! traslado.id)
            traslado.createUser = user
        traslado.updateUser = user
    }

    def getFolio() {
        return Folio.nextFolio('TRASLADOS','TPS')
    }

    def getUser() {
        /*
        if(springSecurityService) {
            def principal = springSecurityService.getPrincipal()
            return principal.username
        }
        */
        return 'NA'
    }

    @Publisher('registrarSalidaPorTps')
    def registrarSalida(Traslado tps){
        log.debug('Afectando inventario por {}: {}', tps.tipo, tps.documento)
        List<Inventario> res = [];
        int renglon = 1
        tps.partidas.each { TrasladoDet det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = tps.sucursal
            inventario.documento = tps.documento
            inventario.cantidad = det.cantidad.abs() * -1
            inventario.comentario = det.comentario
            inventario.fecha = new Date()
            inventario.producto = det.producto
            inventario.tipo = 'TPS'
            inventario.kilos = det.kilos
            inventario.renglon = renglon
            det.inventario = inventario
            inventario.save()
            renglon++
            res.add(inventario)
        }
        tps.fechaInventario  = new Date()
        tps.save()
        return res;
    }

    @Publisher('registrarEntradaPorTpe')
    def registrarEntrada(Traslado tpe){
        log.debug('Afectando inventario por {}: {}', tpe.tipo, tpe.documento)
        List<Inventario> res = [];
        int renglon = 1
        tpe.partidas.each { TrasladoDet det ->
            Inventario inventario = new Inventario()
            inventario.sucursal = tpe.sucursal
            inventario.documento = tpe.documento
            inventario.cantidad = det.cantidad.abs()
            inventario.comentario = det.comentario
            inventario.fecha = new Date()
            inventario.producto = det.producto
            inventario.tipo = 'TPE'
            inventario.kilos = det.kilos
            inventario.renglon = renglon
            det.inventario = inventario
            inventario.save()
            renglon++
            res.add(inventario)
        }
        tpe.fechaInventario  = new Date()
        tpe.save()
        return res;
    }

    def calcularKilos(Producto producto, BigDecimal cantidad){
        def factor = producto.unidad == 'MIL' ? 1000 : 1
        return MonedaUtils.round( (cantidad / factor) * producto.kilos , 3 )
    }

}
