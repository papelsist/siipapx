package com.luxsoft.utils

// import grails.validation.Validateable

// @Validateable
class Mes {
	
	String nombre
	int clave

	static List MESES
	
	static List getMeses() {
		if(!MESES){
			[
				new Mes(nombre:'Enero',clave:0),
				new Mes(nombre:'Febrero',clave:1),
				new Mes(nombre:'Marzo',clave:2),
				new Mes(nombre:'Abril',clave:3),
				new Mes(nombre:'Mayo',clave:4),
				new Mes(nombre:'Junio',clave:5),
				new Mes(nombre:'Julio',clave:6),
				new Mes(nombre:'Agosto',clave:7),
				new Mes(nombre:'Septiembre',clave:8),
				new Mes(nombre:'Octubre',clave:9),
				new Mes(nombre:'Noviembre',clave:10),
				new Mes(nombre:'Diciembre',clave:11),
				
			]
		}
		return MESES
		
	}

	static Mes findMesByNombre(String nombre){
		return getMeses().find{it.nombre.toUpperCase()==nombre.toUpperCase()}
	}
	
	static List getNombres(){
		getMeses().collect{it.nombre}
	}

	static List getClaves(){
		return getMeses().collect
	}

	String toString(){
		return "$nombre ($clave)"
	}
	
	
}


