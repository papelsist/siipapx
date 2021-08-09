package sx.logistica

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import sx.core.InstruccionCorte
import sx.core.Venta
import sx.core.VentaDet
import sx.inventario.SolicitudDeTraslado
import sx.inventario.Traslado
import sx.inventario.TrasladoDet
import sx.security.User

@Transactional
class SurtidoService {

    // @Subscriber
    def onFacturar(Venta venta){
        log.debug("Generando Surtido para factura: ${venta.statusInfo()}")
        // Venta pedido = Venta.findById(source.id, [fetch:[partidas:"join"]])
        if(venta.cuentaPorCobrar)
            generarSurtido(venta)

    }

    // @Subscriber
    def onSave(Venta venta) {
        if(venta.puesto){
            log.debug("Generando Surtido para pedido tipo  PUESTO: {}", venta.getFolio())
            generarSurtido(venta)
        }
    }

    private generarSurtido(Venta venta) {
        Surtido.withNewTransaction {
            Venta pedido = Venta.get(venta.id)
            Surtido found = Surtido.where{ origen == pedido.id}.find()
            if(found) {
                assert found.iniciado == null, 'Pedido con surtido ya iniciado'
                found.delete()
            }
            Surtido surtido = new Surtido()
            surtido.documento = pedido.documento
            surtido.comentario =  pedido.comentario
            surtido.fecha = new Date()
            surtido.clasificacionVale = pedido.clasificacionVale
            surtido.folioFac = pedido.documento
            surtido.nombre = pedido.nombre
            surtido.kilos = pedido.kilos
            surtido.kilosCorte = pedido.partidas.sum 0, { VentaDet det -> det.corte ? det.kilos : 0}
            surtido.folioFac = pedido.cuentaPorCobrar ? pedido.cuentaPorCobrar.documento : 0
            surtido.entidad = pedido.puesto ? 'PST' : 'FAC'
            surtido.origen = pedido.id
            surtido.entregaLocal = pedido.envio ? false : true
            surtido.prods = pedido.partidas.count{ VentaDet det -> det.producto.inventariable && !det.conVale}
            surtido.prodsCorte = pedido.partidas.count { VentaDet det -> det.corte != null}
            surtido.userLastUpdate = User.findByUsername(pedido.updateUser)
            surtido.tipoDeVenta = pedido.tipo
            pedido.partidas.findAll{it.corte}.each { VentaDet det ->
                Corte corte = new Corte()
                corte.producto = det.producto
                corte.instruccionCorte = det.corte
                surtido.addToCortes(corte)
            }
            surtido.save failOnError: true, flush: true
            log.debug('Surtido generado {}', surtido)
            return surtido
        }
    }

    //@Subscriber
    public onGenerarTps(Traslado  origen ){
        log.debug('Atendiendo solicitud de traslado: {}', origen.documento);

        Surtido.withNewTransaction {
            Traslado tps = Traslado.get(origen.id)
            Surtido found = Surtido.where{ origen == tps.id}.find()
            assert found == null, 'TPS con surtido ya iniciado'

            Surtido surtido = new Surtido()
            surtido.documento = tps.documento
            surtido.comentario =  tps.comentario
            surtido.fecha = new Date()
            surtido.clasificacionVale = tps.clasificacionVale
            surtido.folioFac = 0
            surtido.nombre = tps.solicitudDeTraslado.sucursalSolicita.nombre
            surtido.kilos = tps.kilos
            surtido.kilosCorte = 0
            surtido.folioFac = 0
            surtido.entidad = 'TRS'
            surtido.origen = tps.id
            surtido.entregaLocal = false
            surtido.prods = tps.partidas.count{ TrasladoDet det -> det.producto.inventariable}
            surtido.prodsCorte = 0
            surtido.userLastUpdate = User.findByUsername(tps.updateUser) ?: User.first()
            surtido.tipoDeVenta = 'TPS'
            tps.partidas.findAll{it.cortes > 0 }.each { TrasladoDet det ->
                /**
                 *  EN EL MODELO ACTUAL NO SE PUEDE GENERAR LA INSTRUCCION DE CORTE DE POR EL VINCULO CON VENTADET
                Corte corte = new Corte()
                corte.producto = det.producto
                InstruccionCorte instruccionCorte = new InstruccionCorte()
                instruccionCorte.instruccion = det.cortesInstruccion
                instruccionCorte.cantidad = det.cortes
                corte.instruccionCorte = instruccionCorte
                surtido.addToCortes(corte)
                **/
            }
            surtido.save failOnError: true, flush: true
            log.debug('Surtido generado {}', surtido)
            return surtido
        }

    }
}
