package papws.api

import groovy.util.logging.Slf4j

import com.google.cloud.Timestamp

import sx.core.Cliente
import sx.core.ComunicacionEmpresa
import sx.core.Direccion
import sx.core.InstruccionCorte
import sx.core.Producto
import sx.core.Sucursal
import sx.core.Vendedor
import sx.core.Venta
import sx.core.VentaDet
import sx.logistica.CondicionDeEnvio

/*
*
*
*/
@Slf4j
class PedidoConverter {

  PedidoConverter() {}

  Venta mapToVenta(Map<String,Object> pedido, Sucursal sucursal) {
    Cliente cliente = Cliente.get(pedido.cliente.id)
    if(!cliente){
      log.info('Dando de alta cliente nuevo: ', pedido.cliente)
      cliente = crearCliente(pedido)
    }
    Timestamp fechaFb = pedido.fecha

    Date fecha = fechaFb.toDate()
    Integer folio = pedido.folio as Integer
    Date dateCreated = pedido.dateCreated.toDate()
    Date lastUpdated = pedido.lastUpdated.toDate()

    Vendedor vendedor = Vendedor.findByNombres('CASA')
    Venta venta = new Venta()
    venta.cliente = cliente
    venta.sucursal  = sucursal
    venta.cargosPorManiobra = pedido.cargosPorManiobra
    venta.cfdiMail = pedido.cfdiMail
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
    venta.fecha = pedido.fecha.toDate()
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

    venta.documento = pedido.folio
    venta.sw2 = pedido.id
    venta.vendedor = vendedor
    venta.atencion = 'TELEFONICA'
    venta.callcenter = true

    agregarPartidas(venta, pedido, sucursal)

    if(pedido.envio){
      def condicion = crearCondicionEnvio(pedido.envio)
      venta.envio = condicion
      condicion.venta = venta
    }
    return venta
  }

  Cliente crearCliente( pedido, Sucursal, sucursal){

    Map<String,Object> clienteFb = pedido.cliente

    Cliente cliente = new Cliente()
    cliente.clave = clienteFb.clave
    cliente.rfc = clienteFb.rfc
    cliente.nombre = clienteFb.nombre
    cliente.email= clienteFb.email
    cliente.createUser = pedido.createUser
    cliente.updateUser = pedido.createUser
    cliente.sucursal = sucursal
    Direccion direccion = crearDireccionCliente(clienteFb)
    cliente.direccion = direccion
    cliente.id = clienteFb.id

    Map<String,Object> medios = clienteFb.medios
    if(medios){
      medios.each{
        def medio = new ComunicacionEmpresa()
        medio.id = it.id
        medio.tipo = it.tipo
        medio.activo = it.activo
        medio.cfdi = it.cfdi
        medio.comentario = ''
        medio.cliente = cliente
        medio.createUser = pedido.createUser
        medio.updateUser = pedido.createUser
        medio.sucursalCreated = sucursal.nombre
        medio.sucursalUpdated = sucursal.nombre
        medio.validado = true
        medio.descripcion = it.descripcion
        cliente.addToMedios(medio)
      }
    }
    return cliente
  }

  Direccion crearDireccionCliente(clienteFb){
    Map<String,Object> direccionFb = clienteFb.direccion
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

  void agregarPartidas(Venta venta, Map<String,Object> pedido, Sucursal sucursal) {
    pedido.partidas.each {partida ->

      VentaDet ventaDet = crearVentaDet(partida, sucursal)
      venta.addToPartidas(ventaDet)
      if(partida.clave == "MANIOBRAF"){
        venta.cargosPorManiobra = partida.subtotal
      }
    }
  }

  VentaDet crearVentaDet(Map<String,Object> partida, Sucursal sucursal){

    Producto producto = Producto.get(partida.productoId)
    VentaDet ventaDet = new VentaDet()
    ventaDet.sw2 = partida.id
    ventaDet.sucursal = sucursal
    ventaDet.producto  = producto
    ventaDet.comentario = partida.comentario
    ventaDet.kilos = partida.kilos
    ventaDet.descuentoOriginal = partida.descuentoOriginal
    ventaDet.precioOriginal = partida.precioOriginal
    ventaDet.importeCortes = partida.importeCortes
    ventaDet.nacional = partida.nacional
    ventaDet.precioLista = partida.precioLista
    ventaDet.impuestoTasa =  partida.impuestoTasa
    ventaDet.descuento = partida.descuento

    ventaDet.cantidad = partida.cantidad
    ventaDet.precio = partida.precio
    ventaDet.descuentoImporte = partida.descuentoImporte.toBigDecimal()
    ventaDet.importe = partida.importe.toBigDecimal()
    ventaDet.subtotal = partida.subtotal.toBigDecimal()
    ventaDet.impuesto = partida.impuesto.toBigDecimal()
    ventaDet.total = partida.total.toBigDecimal()

    if(partida.corte){
      // Crear Intstruccion de corte
      InstruccionCorte corte = creaInstruccionCorte(partida.corte)
      ventaDet.corte = corte
      corte.ventaDet = ventaDet
    }
    return ventaDet
  }

  InstruccionCorte creaInstruccionCorte(Map<String,Object> fbcorte){
    InstruccionCorte corte = new InstruccionCorte()
    corte.cantidad = fbcorte.cantidad
    corte.precio = fbcorte.precio
    corte.instruccion = fbcorte.instruccion
    corte.refinado = fbcorte.refinado
    return corte
  }

  CondicionDeEnvio crearCondicionEnvio(Map<String,Object> envio){

    Direccion direccion = new Direccion()
    direccion.calle = envio.direccion.calle
    direccion.numeroInterior = envio.direccion.numeroInterior
    direccion.numeroExterior = envio.direccion.numeroExterior
    direccion.colonia = envio.direccion.colonia
    direccion.municipio = envio.direccion.municipio
    direccion.codigoPostal = envio.direccion.codigoPostal
    direccion.estado = envio.direccion.estado

    CondicionDeEnvio condicion = new CondicionDeEnvio()
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
}
