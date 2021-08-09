package sx.cxc


import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.exception.ExceptionUtils


class RevisionService {

    def buscarPendientes(){
        def rows = VentaCredito.findAll(
                "from VentaCredito v  " +
                        " where  v.cuentaPorCobrar.tipo = ? " +
                        " and v.cuentaPorCobrar.total - v.cuentaPorCobrar.pagos > 0 " +
                        " and v.cuentaPorCobrar.cfdi.uuid is not null " +
                        " and v.cuentaPorCobrar.cancelada is null" +
                        " order by v.cuentaPorCobrar.fecha desc",
                ['CRE'])
        return rows
    }

    /**
     * Genera las entidades de VentaCredito para todas las cuentas por cobrar que lo requieran
     *
     * @return Lista de entidades VentaCredito generadas
     */
    List<VentaCredito> generar() {
        List<CuentaPorCobrar> rows = CuentaPorCobrar.findAll(
                "from CuentaPorCobrar c  " +
                        " where  c.tipo = ? " +
                        " and c.total - c.pagos > 0 " +
                        " and c.cfdi.uuid is not null " +
                        " and c.credito is null " +
                        " and c.cancelada is null" +
                        " order by c.fecha desc",
                ['CRE'])
        List<VentaCredito> generated = []
        rows.each {
            try{
                generated << generarVentaCredito(it)
            }catch (Exception ex) {
                String msg = ExceptionUtils.getRootCauseMessage(ex)
                log.debug('Error al generar registrar revision y cobro para : {}', it.id)
                log.debug('Error: {}', msg)
            }
        }
        return generated
    }

    /**
     *
     * @param cxc La cuenta por cobrar
     * @return La VentaCredito correspondiente
     */
    @Transactional
    VentaCredito generarVentaCredito(CuentaPorCobrar cxc) {
        if(cxc.credito)
            return cxc.credito
        VentaCredito credito = new VentaCredito()
        // Propiedades
        credito.diaPago = cxc.cliente.credito.diaCobro
        credito.diaRevision = cxc.cliente.credito.diaRevision
        credito.plazo = cxc.cliente.credito.plazo
        credito.revisada = false
        credito.revision = cxc.cliente.credito.revision
        credito.cobrador = cxc.cliente.credito.cobrador
        credito.socio = cxc.cliente.credito.socio
        // Fechas
        Date vto = cxc.fecha + cxc.cliente.credito.plazo.intValue()
        cxc.vencimiento = vto
        credito.vencimiento = vto

        // Se congela la fecha original de revision
        credito.fechaRevisionCxc = getProximaRevision(cxc.fecha, cxc.cliente.credito.diaRevision.intValue())
        credito.fechaRevision = credito.fechaRevisionCxc

        // Se congela la fecha orignal de pago
        credito.fechaPago = getProximoPago(vto, cxc.cliente.credito.diaCobro.intValue())
        credito.reprogramarPago = credito.fechaPago

        cxc.credito = credito
        cxc.save failOnError: true, flush: true
        return credito
    }

    /**
     * Actualiza los correspondientes a reviision y cobro para las cuentas por cobrar
     *
     * @return Lista de ventaCredito de las cuentas actualizadas
     */
    public List<VentaCredito> actualizar(){

        def rows = VentaCredito.findAll(
                "from VentaCredito c  " +
                        " where c.cuentaPorCobrar.tipo = ? " +
                        " and c.revision = true " +
                        " and c.cuentaPorCobrar.total - c.cuentaPorCobrar.pagos > 0 " +
                        " and c.cuentaPorCobrar.uuid is not null " +
                        " order by c.cuentaPorCobrar.cliente.nombre asc",
                ['CRE'])
        List<VentaCredito> res = []
        rows.each { VentaCredito credito ->
            res << actualizarRevision(credito)
        }
        return res
    }

    /**
     * Actualia los datos de revision y cobro de la cuenta por cobrar
     *
     * @param credito VentaCredito a actualizar
     * @return
     */
    public VentaCredito actualizarRevision(VentaCredito credito) {
        Date hoy = new Date()
        Integer diaRevision = credito.diaRevision
        Integer diaPago = credito.diaPago
        if(!credito.revisada) {
            credito.fechaRevision = getProximaRevision(hoy, diaRevision)
        }
        credito.reprogramarPago = getProximoPago(hoy, diaPago)
        if(credito.fechaRevision >= credito.reprogramarPago) {
            credito.reprogramarPago = getProximoPago(credito.vencimiento, diaPago)
        }
        credito.save failOnError: true, flush: true
        return credito
    }

    def getProximaRevision(def fecha, Integer diaRevision){
        Integer dia = (fecha[Calendar.DAY_OF_WEEK] - 1)
        Integer dif = dia - diaRevision
        if (dif >= 0){
            def faltantes = 7 - dif
            def proximo = fecha + faltantes
            return proximo
        } else {
            def proximo = fecha + dif.abs()
            return proximo
        }
    }

    def getProximoPago(def fecha, int diaCobro){
        def dia = (fecha[Calendar.DAY_OF_WEEK] - 1)
        def dif = dia - diaCobro
        if(dif > 0 ) {
            def faltantes = 7 - dif
            def proxima = fecha + faltantes
            return proxima
        } else {
            def proxima = fecha + dif.abs()
            return proxima
        }
    }





}
