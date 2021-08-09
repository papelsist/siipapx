package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class PreciosPorClienteController extends RestfulController{

    PreciosPorClienteController(){
        super(PreciosPorCliente)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('Buscando precios por cliente {}', params)
        params.max = 100
        Cliente cliente = Cliente.get(params.cliente)
        def list = PreciosPorCliente.where { cliente == cliente && activo }.list()
        respond list
    }

    def buscarPrecio() {
        log.debug('Buscando precio por cliente: {}', params)
        Cliente cliente = Cliente.get(params.cliente)
        Producto producto = Producto.get(params.producto)
        PreciosPorClienteDet precio = PreciosPorClienteDet.where{
            preciosPorCliente.cliente == cliente && producto == producto }.find()
        respond precio
    }

    def preciosPorCliente() {
        Cliente cliente = Cliente.get(params.cliente)
        log.debug('Buscando precio por cliente: {}', cliente)
        List precios = PreciosPorClienteDet.where{
            preciosPorCliente.cliente == cliente }.list()
        respond precios
    }
}
