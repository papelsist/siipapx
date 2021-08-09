package pos.server

import sx.core.Venta

class DepurarPedidosJob {
    static triggers = {
        cron name:   'depPed',   startDelay: 10000, cronExpression: '0 0 21 * * ?'
    }

    def execute() {
        def pedidos=Venta.where{sucursal == sucursal && cuentaPorCobrar == null && facturar == null && fecha < (new Date()-45) && noFacturable==false}
        pedidos.each{ pedido ->
            pedido.noFacturable=true;
            pedido.save failOnError:true, flush:true

        }
    }
}
