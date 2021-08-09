package sx.core

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import sx.core.Folio

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ClienteController extends RestfulController{

    static responseFormats = ['json']

    ClienteController(){
        super(Cliente)
    }
    
    @Override
    protected List listAllResources(Map params) {
        def query = Cliente.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        if(params.term){
            def search = '%' + params.term + '%'
            //query = query.where { clave =~ search || nombre =~ search}
            query = query.where { nombre =~ search}
        }
        if(params.activo){
            query = query.where { activo == params.activo}
        }
        return query.list(params)
    }

    protected Cliente saveResource(Cliente resource) {
        def username = getPrincipal().username
        
        if(resource.id == null) {
            def sucursal = Sucursal.get(params.sucursal)
            assert sucursal, 'No existe la sucursal ' + params.sucursal
            def serie = sucursal.nombre
            def fol = Folio.nextFolio('CLIENTES',serie).toString()
            fol = fol.padLeft(7, '0')
            def clave = "SX${serie.substring(0,2)}${fol}"
            log.debug('Clave generada para cliente nuevo {} : {} ', resource.nombre, clave);
            resource.clave = clave
            resource.id = UUID.randomUUID().toString()
            resource.createUser = username
            if (resource.vendedor == null) {
                resource.vendedor = Vendedor.where { nombres == 'CASA'}.find()
            }
            if(resource.medios){
                resource.medios.each{
                    it.id = UUID.randomUUID().toString()
                }
            }
        }
        resource.updateUser = username
        return super.saveResource(resource)
    }

    def actualizarCfdiMail(Cliente cliente) {
        if (cliente == null) {
            notFound()
            return
        }
        String email = params.email
        Sucursal sucursal = AppConfig.first().sucursal
        String usuario = params.usuario
        def medio = cliente.medios.find {it.tipo =='MAIL' && it.cfdi}
        log.debug('Medio localizado: {}', medio)
        if(!medio) {
            medio = new ComunicacionEmpresa()
            medio.id = UUID.randomUUID().toString()
            medio.tipo = 'MAIL'
            medio.activo = true
            medio.cfdi = true
            medio.comentario = 'email para envio de CFDIs'
            medio.cliente = cliente
            medio.createUser = usuario
            medio.updateUser = usuario
            medio.sucursalCreated = sucursal.nombre
            medio.sucursalUpdated = sucursal.nombre
            medio.validado = true
            medio.updateUser = usuario
            cliente.addToMedios(medio)
        }
        medio.descripcion = email
        medio.updateUser = usuario
        medio.sucursalUpdated = sucursal.nombre
        medio.validado = true
        cliente.save failOnError: true, flush:true
        respond cliente

    }

     def actualizarTelefono(Cliente cliente) {
        if (cliente == null) {
            notFound()
            return
        }

        println 'Actualizando el telefono '+cliente.nombre

        def medios = cliente.medios.findAll{ it.tipo == 'TEL'}
    
        def medio = null
        if(medios){
            medio = medios?.sort{it.id}?.first()
        }

        String telefono = params.telefono
        println "Telefono: " +telefono
        Sucursal sucursal = AppConfig.first().sucursal
        String usuario = params.usuario

         if(!medio) {
            medio = new ComunicacionEmpresa()
            medio.id = UUID.randomUUID().toString()
            medio.tipo = 'TEL'
            medio.activo = true
            medio.cfdi = false
            medio.comentario = 'TELEFONO 1'
            medio.cliente = cliente
            medio.createUser = usuario
            medio.updateUser = usuario
            medio.sucursalCreated = sucursal.nombre
            medio.sucursalUpdated = sucursal.nombre
            medio.validado = true
            medio.updateUser = usuario
            cliente.addToMedios(medio)
        }
        
        medio.descripcion = telefono
        medio.updateUser = usuario
        medio.sucursalUpdated = sucursal.nombre
        medio.validado = false
        cliente.save failOnError: true, flush:true
        
        respond cliente

    }

    def validarRfc(){
        String rfc = params.rfc
        Cliente found = Cliente.where {rfc == rfc}.find()
        respond found?: []
    }
}
