package sx.security

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User implements Serializable {

    private static final long serialVersionUID = 1

    transient springSecurityService

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    String apellidoPaterno
    String apellidoMaterno
    String nombres
    String nombre
    Integer numeroDeEmpleado
    String email
    String sucursal
    String puesto
    String nip

    User() {}

    User(String username, String password) {
        //this()
        this.username = username
        this.password = password
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this)*.role
    }

    def beforeInsert() {
        encodePassword()
        capitalizarNombre()
    }



    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
        if (isDirty('apellidoPaterno') || isDirty('apellidoMaterno') || isDirty('nombres')) {
            capitalizarNombre()

        }
    }

    protected void encodePassword() {
        nip = password
        password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
    }

    private capitalizarNombre(){
        apellidoPaterno=apellidoPaterno.toUpperCase()
        apellidoMaterno=apellidoMaterno.toUpperCase()
        nombres=nombres.toUpperCase()
        nombre="$nombres $apellidoPaterno $apellidoMaterno"
    }

    static transients = ['springSecurityService']

    static constraints = {
        username blank: false, unique: true
        password blank: false

        email nullable:true,email:true
        numeroDeEmpleado nullable:true
        sucursal nullable:true,maxSize:20
        puesto nullable:true,maxSize:30
        nip nullable: true

    }

    static mapping = {
        password column: '`password`'
    }
}
