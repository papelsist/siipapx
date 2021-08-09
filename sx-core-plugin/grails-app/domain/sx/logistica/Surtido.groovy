package sx.logistica

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import sx.security.User

@EqualsAndHashCode(includes='nombre')
@ToString( includes = "origen,tipo,nombre,documento",includeNames=true,includePackage=false)
class Surtido {
   
    String id

    String origen

    String tipo

    String nombre

    String comentario

    Boolean	entregaLocal = true

    Boolean	parcial	 = false

    Long documento = 0

    String tipoDeVenta

    Date fecha

    Long folioFac = 0

    User facturo

    String clasificacionVale

    User asignado

    Date inicio

    User cerro

    Date cerrado

    User reviso

    Date revisado

    User entrego

    Date entregado

    User depuro

    Date depurado

    User cancelo

    Date cancelado

    User cortador

    Date asignadoCorte

    Date corteInicio

    Date corteFin

    BigDecimal kilos = 0

    Integer	prods = 0

    BigDecimal tiempoSurtido = 0

    BigDecimal kilosCorte = 0

    Integer	prodsCorte = 0

    BigDecimal tiempoCorte = 0

    String estado

    User autorizo

    Date dateCreated
    Date lastUpdated


    List<Corte> cortes =[]

    List<AuxiliarCorte> auxiliares= []

    List<SurtidoDet> parciales= []


    static hasMany = [cortes:Corte,auxiliares :AuxiliarSurtido, parciales: SurtidoDet]


    static constraints = {

        comentario nullable: true
        asignado nullable: true
        inicio nullable: true
        corteFin nullable: true
        corteInicio nullable: true
        asignadoCorte nullable: true
        cerro   nullable: true
        cerrado nullable: true
        reviso nullable: true
        revisado nullable: true
        entrego nullable: true
        entregado nullable: true
        depuro nullable: true
        depurado nullable: true
        tiempoSurtido nullable: true
        kilosCorte nullable: true
        prodsCorte nullable: true
        cortador nullable: true
        tiempoCorte nullable: true
        tipo inList:['PST','FAC','SOL','TRS']
        cancelado nullable: true
        cancelo nullable: true
        cortes nullable: true
        auxiliares nullable: true
        folioFac nullable:true
        facturo nullable:true
        parciales nullable: true
        autorizo nullable: true

    }

    static mapping = {
        id generator: 'uuid'
        auxiliares cascade: "all-delete-orphan"
        cortes cascade: "all-delete-orphan"
        parciales cascade: "all-delete-orphan"
    }

}
