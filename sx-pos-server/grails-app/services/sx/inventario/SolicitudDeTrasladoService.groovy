package sx.inventario

import grails.events.annotation.Publisher
import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import sx.core.Folio
import sx.core.Venta
import sx.core.VentaDet
import sx.logistica.Chofer


@Transactional
class SolicitudDeTrasladoService {

    SpringSecurityService springSecurityService
    TrasladoService trasladoService

    @Publisher
    def atender(SolicitudDeTraslado sol, Chofer chofer, String comentario) {
        sol = sol.save()
        Map map = [:]
        map.sol = sol.id
        map.chofer = chofer
        map.comentario = comentario
        return map
    }

    @Subscriber
    def onMandarFacturar(Venta venta) {
        if(venta.clasificacionVale != 'SIN_VALE' && venta.clasificacionVale != 'EXISTENCIA_VENTA') {
            log.debug (' Generando  vale automatico tipo: {} para: {}', venta.clasificacionVale, venta.statusInfo() )
            def sol = generarValeAutomatico(venta.id);
        }
    }

    def generarValeAutomatico( String ventaId) {
        Venta.withNewTransaction {
            Venta venta = Venta.get(ventaId)
            List partidas = venta.partidas.findAll{ it.conVale }
            if (partidas) {
                // log.debug('Partidas para el vale: {}', partidas.size())
                SolicitudDeTraslado sol = new SolicitudDeTraslado()
                sol.clasificacionVale = venta.clasificacionVale
                sol.documento = getFolio()
                sol.venta = venta.id
                sol.comentario =  "${venta.statusInfo()} ${venta.clasificacionVale}"
                sol.fecha = new Date()
                sol.sucursalSolicita = venta.sucursal
                sol.sucursalAtiende = venta.sucursalVale
                sol.referencia = "${venta.tipo} - ${venta.documento}"
                partidas.each { VentaDet det ->
                    SolicitudDeTrasladoDet solDet = new SolicitudDeTrasladoDet()
                    solDet.producto = det.producto
                    solDet.comentario = "${venta.statusInfo()} ${venta.clasificacionVale}"
                    solDet.solicitado = det.cantidad
                    if(det.corte) {
                        solDet.cortesInstruccion = det.corte.instruccion
                        solDet.cortes = det.corte.cantidad
                    }
                    sol.addToPartidas(solDet)
                }
                sol.updateUser = venta.updateUser
                sol.createUser = venta.createUser
                sol.save()
                return sol
            } else {
                log.debug( 'La venta no tiene partidas que califiquen para vale')
            }

        }
    }

    def logEntity(SolicitudDeTraslado sol) {
        def user = getUser()
        if(! sol.id)
            sol.createUser = user
        sol.updateUser = user
    }

    def getUser() {
        def principal = springSecurityService.getPrincipal()
        return principal.username
    }

    /**
     * Genera el folio para las solicitudes
     *
     * @return
     */
    def getFolio() {
        return Folio.nextFolio('SOLS','SOLS')
    }

}
