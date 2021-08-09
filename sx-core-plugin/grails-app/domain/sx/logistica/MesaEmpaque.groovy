package sx.logistica

import grails.databinding.BindingFormat

import sx.security.User

class MesaEmpaque {

    String id

    @BindingFormat('dd/MM/yyyy')
    Date fecha

    User cortador

    User empacador1

    User empacador2

    User empacador3

    User empacador4

    User empacador5

    User empacador6

    User empacador7

    User empacador8

    Date dateCreated
    Date lastUpdated


    static constraints = {

        empacador1 nullable: true
        empacador2 nullable: true
        empacador3 nullable: true
        empacador4 nullable: true
        empacador5 nullable: true
        empacador6 nullable: true
        empacador7 nullable: true
        empacador8 nullable: true

    }

    static mapping={
        id generator:'uuid'
    }
}
