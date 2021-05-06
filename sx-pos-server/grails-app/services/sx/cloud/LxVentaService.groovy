package sx.cloud

import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.annotation.PreDestroy

import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import grails.gorm.transactions.Transactional

import com.google.firebase.cloud.*
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Query
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.DocumentSnapshot

import com.google.cloud.firestore.ListenerRegistration 
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.EventListener;


import sx.core.AppConfig
import sx.core.Sucursal
import sx.core.Cliente
import sx.core.Vendedor
import sx.core.Direccion
import sx.core.InstruccionCorte
import sx.core.Venta
import sx.core.VentaDet
import sx.core.Producto
import sx.logistica.CondicionDeEnvio
import sx.core.ComunicacionEmpresa


@Slf4j
@Transactional
class LxVentaService implements ApplicationListener<ContextRefreshedEvent>, EventListener<QuerySnapshot> {

    static lazyInit = false

    FirebaseService firebaseService
    ListenerRegistration registration
    def suc

    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
        start()
    }
    

    def ventaCreate(DocumentChange documentChange) {
 
        def suc = AppConfig.first().sucursal

        DocumentSnapshot pedidoSnapshot =documentChange.getDocument()
        Map<String,Object> pedido = pedidoSnapshot.getData() 

        try{
            pedidoToVenta(pedido, suc, pedidoSnapshot)
        }catch( Exception e ){
            e.printStackTrace();
            log.error(e)
        }

    }

    def pedidoToVenta(Map<String,Object> pedido, Sucursal suc, def pedidoSnapshot) {
        log.debug('Generando Venta para el pedido {}', pedido.folio)
        // Creacion de la Venta
        Venta venta = crearVenta(pedido, suc)
        
        //Validar si tiene partidas para crear VentaDet
        if(pedido.partidas){
            pedido.partidas.each {partida ->

                // Crear Venta Det
                def ventaDet = crearVentaDet(partida, suc)
                
                // Validar si tiene corte para crear InstruccionDeCorte
                if(partida.corte){
                    
                    // Crear Intstruccion de corte
                    InstruccionCorte corte = creaInstruccionCorte(partida)
                    ventaDet.corte = corte
                    corte.ventaDet = ventaDet
                }
                
                if(partida.clave == "MANIOBRAF"){
                    venta.cargosPorManiobra = partida.subtotal
                }


                venta.addToPartidas(ventaDet)
            }
        }
        // Validar si tiene envio para crear CondicionDeEnvio
        if(pedido.envio){
            def condicion = crearCondicionEnvio(pedido.envio)
            venta.envio = condicion
            condicion.venta = venta
        }
        
       venta.save failOnError:true,flush:true

        Map<String, Object> update = new HashMap<>();
        update.put("status", "FACTURABLE");
                    
        DocumentReference pedidoRef = pedidoSnapshot.getReference()
        ApiFuture<WriteResult> writeResult = pedidoRef.set(update, SetOptions.merge())

        
        firebaseService.updateCollection('pedidos_log', pedido.id, update)

    }




    def crearVenta(Map<String,Object> pedido, def sucursalLocal) {
     
       // def cliente = Cliente.findByNombre(pedido.nombre)
        def cliente = Cliente.get(pedido.cliente.id)
        if(!cliente){
           cliente = crearCliente(pedido, sucursalLocal)
        }
        def vendedor = Vendedor.findByNombres('CASA')
        def venta = new Venta()

        venta.cargosPorManiobra = pedido.cargosPorManiobra
        venta.cfdiMail = pedido.cfdiMail
        venta.cliente = cliente
        venta.comentario = pedido.comentario
        venta.comisionTarjeta = pedido.comisionTarjeta
        venta.comisionTarjetaImporte = pedido.comisionTarjetaImporte
        venta.comprador = pedido.comprador
        venta.corteImporte = pedido.corteImporte
        venta.createUser = pedido.createUser
        venta.updateUser = pedido.updateUser
        venta.descuento = pedido.descuento
        venta.descuentoOriginal = pedido.descuento
        venta.descuentoImporte = pedido.descuentoImporte
        venta.descuentoOriginal = pedido.descuento
        venta.documento = pedido.folio
        venta.fecha = pedido.fecha
        venta.formaDePago = pedido.formaDePago
        venta.importe = pedido.importe
        venta.impuesto = pedido.impuesto
        venta.kilos = pedido.kilos
        venta.moneda = Currency.getInstance(pedido.moneda)
        venta.tipo = pedido.tipo
        if(pedido.tipo == "COD"){
            venta.cod = true 
        }
        venta.nombre = pedido.nombre
        venta.subtotal = pedido.subtotal
        venta.total = pedido.total
        venta.tipoDeCambio = pedido.tipoDeCambio
        venta.usoDeCfdi = pedido.usoDeCfdi
        venta.sucursal  = sucursalLocal
        venta.documento = pedido.folio
        venta.sw2 = pedido.id
        venta.vendedor = vendedor
        venta.atencion='TELEFONICA'
        venta.callcenter = true
        return venta
    }



    def crearVentaDet(Map<String,Object> partida, def sucursalLocal ){
    
        Producto producto = Producto.get(partida.producto)

        def ventaDet = new VentaDet()

        ventaDet.sucursal = sucursalLocal
        ventaDet.total = partida.total
        ventaDet.cantidad = partida.cantidad
        ventaDet.precio = partida.precio
        ventaDet.precioOriginal = partida.precioOriginal
        ventaDet.kilos = partida.kilos
        ventaDet.descuentoOriginal = partida.descuentoOriginal
        ventaDet.importeCortes = partida.importeCortes
        ventaDet.descuentoImporte = partida.descuentoImporte
        ventaDet.descuento = partida.descuento
        ventaDet.nacional = partida.nacional
        ventaDet.precioLista = partida.precioLista
        ventaDet.comentario = partida.comentario
        ventaDet.producto  = producto
        ventaDet.impuesto = partida.impuesto
        ventaDet.subtotal = partida.subtotal
        ventaDet.impuestoTasa =  partida.impuestoTasa
        ventaDet.importe = partida.importe
        ventaDet.sw2 = partida.id

    
        
        return ventaDet
    }

    def creaInstruccionCorte(Map<String,Object> partida){
        
        InstruccionCorte corte = new InstruccionCorte()
        corte.cantidad = partida.corte.cantidad
        corte.precio = partida.corte.precio
        corte.instruccion = partida.corte.instruccion
        corte.refinado = partida.corte.refinado
        return corte
    }

    def crearCondicionEnvio(Map<String,Object> envio){
        
        def direccion = new Direccion()
        
        direccion.calle = envio.direccion.calle
        direccion.numeroInterior = envio.direccion.numeroInterior
        direccion.numeroExterior = envio.direccion.numeroExterior
        direccion.colonia = envio.direccion.colonia
        direccion.municipio = envio.direccion.municipio
        direccion.codigoPostal = envio.direccion.codigoPostal
        direccion.estado = envio.direccion.estado
        
        def condicion = new CondicionDeEnvio()
        
        condicion.direccion = direccion
        condicion.comentario = envio.comentario
        if(envio.fechaDeEntrega){
            condicion.fechaDeEntrega = envio.fechaDeEntrega.toDate()
        }
        if(envio.transporte){
            condicion.transporte = envio.transporte
        }
        condicion.condiciones = "Tipo: "+envio.tipo+" Contacto: "+envio.contacto+" Tel:"+envio.telefono+" Horario: "+envio.horario
        
        if(envio.tipo == 'OCURRE'){
            condicion.ocurre = true
        }


        return condicion 
    }
    
    def crearCliente( pedido, sucursalLocal){

        def clienteFb = pedido.cliente

        def cliente = new Cliente()
    	
        cliente.clave = clienteFb.clave
    	cliente.rfc = clienteFb.rfc
    	cliente.nombre = clienteFb.nombre
        cliente.email= clienteFb.email
        cliente.createUser = pedido.createUser
        cliente.updateUser = pedido.createUser
        cliente.sucursal = sucursalLocal
        def direccion = crearDireccionCliente(clienteFb)
        cliente.direccion = direccion   
        cliente.id = clienteFb.id
        
        if(clienteFb.medios){
            clienteFb.medios.each{
               def medio = new ComunicacionEmpresa()
                medio.id = it.id
                medio.tipo = it.tipo
                medio.activo = it.activo
                medio.cfdi = it.cfdi
                medio.comentario = ''
                medio.cliente = cliente
                medio.createUser = pedido.createUser
                medio.updateUser = pedido.createUser
                medio.sucursalCreated = sucursalLocal.nombre
                medio.sucursalUpdated = sucursalLocal.nombre
                medio.validado = true
                medio.descripcion = it.descripcion
                cliente.addToMedios(medio)
            }
        }
        
        cliente.save failOnError:true,flush:true
        return cliente
    }


    Direccion crearDireccionCliente(clienteFb){

        def direccionFb = clienteFb.direccion
        def direccion = new Direccion()
        direccion.calle = direccionFb.calle
        direccion.numeroInterior = direccionFb.numeroInterior
        direccion.numeroExterior = direccionFb.numeroExterior
        direccion.colonia = direccionFb.colonia
        direccion.municipio = direccionFb.municipio
        direccion.codigoPostal = direccionFb.codigoPostal
        direccion.estado = direccionFb.estado

        return direccion

    }

    void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException ex) {
        if(ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error("Error: {}", msg, ex)
        }
        // log.info('Changes detected: {}', snapshots)
        snapshots.getDocumentChanges().each { DocumentChange dc ->
            log.info('Document: {} Type: {}', dc.getDocument().getData().folio, dc.type)
            switch (dc.type) {
                case 'ADDED':
                    //println('Se agrego un pedido: ')
                    ventaCreate(dc)
                    break
                case 'MODIFIED':
                    // ventaCreate(dc)
                    break
                case 'REMOVED':
                    break
            }
            /*
            if(dc.type == 'ADDED') {
                log.debug('Detectando nuevo pedido')
                ventaCreate(dc)
            }
            */
        }
        
    }

    /*
    def listenerRegistration(){

        Firestore db = firebaseService.getFirestore()
        def suc = AppConfig.first().sucursal

		registration = db.collection("pedidos").whereEqualTo('sucursal', suc.nombre)
                .whereEqualTo('status', 'CERRADO2')
				.addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			void onEvent(
					@Nullable QuerySnapshot snapshots,
					@Nullable FirestoreException ex) {
				if(ex) {
					String msg = ExceptionUtils.getRootCauseMessage(ex)
                    log.error(msg, ex)
				}
                
				snapshots.getDocumentChanges().each { DocumentChange dc ->
                    if(dc.type == 'ADDED') {
                        ventaCreate(dc)
                    }
				}
			}
		})
        log.info('Listening to firestore collection: {}', 'pedidos')
    }
    */

    def start() {
        suc = AppConfig.first().sucursal
        Firestore db = firebaseService.getFirestore()
        registration = db.collection('pedidos')
        .whereEqualTo('sucursal', suc.nombre)
        .whereEqualTo('status', 'CERRADO')
        .addSnapshotListener(this)
        log.debug('Listening to firestore collection: {}', 'pedidos')
    }   

    @PreDestroy
    def stop(){
        if(registration) {
            registration.remove()
            log.debug('Firbase listener for collection: {} removed' , 'pedidos')
        }
    }

}
