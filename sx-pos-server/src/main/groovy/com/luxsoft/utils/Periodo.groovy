package com.luxsoft.utils

import grails.web.databinding.WebDataBinding


// import grails.validation.Validateable;

import java.text.DateFormat
import java.text.SimpleDateFormat

// @Validateable
class Periodo implements Comparable<Periodo> , WebDataBinding{
	
	
	Date fechaInicial
	Date fechaFinal
	
	static String defaultFormat='dd/MM/yyyy'
	
	static constraints = {
		fechaInicial()
		fechaFinal(nullable:false,validator:{val,object->
			if(val<object.fechaInicial)
				return 'fechaFinal.anteriorAFechaInicial'
			else
				return true
		})
	}
	
	static transients = ['listaDeDias']
	
	Periodo(){
		fechaInicial=new Date()
		fechaFinal=new Date()
	}
	
	Periodo(String f1,String f2){
		fechaInicial=Date.parse(defaultFormat, f1).clearTime()
		fechaFinal=Date.parse(defaultFormat,f2).clearTime()
	}
	
	Periodo(Date f1,Date f2){
		fechaInicial=f1
		fechaFinal=f2
	}

	def List<Periodo> toMeses(){
		return periodosMensuales(this)
	}
	
	String toString(){
		"${fechaInicial?.format(defaultFormat)} - ${fechaFinal?.format(defaultFormat)}"
	}

	String mothLabel(){
		"${fechaInicial?.format('MMMM')} - ${fechaFinal?.format('MMM')}"
	}
	
	def int dias(){
		return fechaFinal-fechaInicial
	}
	
	public List<Date> getListaDeDias(){
		final List<Date> list=new ArrayList<Date>();
		final Calendar calendar=Calendar.getInstance();
		calendar.setTime(this.fechaInicial);
		Date fecha=this.getFechaInicial();
		while(fecha.compareTo(getFechaFinal())<=0){
			list.add(fecha);
			calendar.add(Calendar.DATE,1);
			fecha=calendar.getTime();
		}
		return list;
	}
	
	

	@Override
	public int compareTo(Periodo p2) {
		// TODO Auto-generated method stub
		if(fechaInicial.equals(p2.fechaInicial)){
			return fechaFinal.compareTo(p2.fechaFinal);
		}
		return fechaInicial.compareTo(p2.fechaInicial);
	}
	
	public static Periodo getCurrentMonth() {
		Calendar now=Calendar.getInstance()
		return getPeriodoEnUnMes(now.get(Calendar.MONTH),now.get(Calendar.YEAR))
	}

	/**
	* Regresa el periodo correspondiente a un mes 
	**/
	static Periodo fromMes(int mes){
		return getPeriodoEnUnMes(mes)
	}
	
	public static Periodo getPeriodoEnUnMes(int mes){
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.MONTH,mes);
		cal.set(Calendar.DATE,1);
		Date start=cal.getTime();
		int last=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DATE,last);
		Date end=cal.getTime();
		Periodo p=new Periodo(fechaInicial:start,fechaFinal:end);
		return p;
	}
	public static Periodo getPeriodoEnUnMes(int mes,int ano){
		Calendar cal=Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR,ano);
		cal.set(Calendar.MONTH,mes);
		cal.set(Calendar.DATE,1);
		
		Date start=cal.getTime();
		int last=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DATE,last);
		
		Date end=cal.getTime();
		Periodo p=new Periodo(fechaInicial:start,fechaFinal:end);
		return p;
	}
	
	public static Periodo getPeriodoAnual(int year){
		Periodo p1=getPeriodoEnUnMes(0,year);
		Periodo p2=getPeriodoEnUnMes(11,year);
		Periodo p=new Periodo(fechaInicial:p1.fechaInicial,fechaFinal:p2.fechaFinal);
		return p;
	}
	
	public static List<Periodo> getPeriodosDelYear(int year){
		Periodo p1=getPeriodoEnUnMes(0,year);
		Periodo p2=getPeriodoEnUnMes(11,year);
		Periodo p=new Periodo(p1.getFechaInicial(),p2.getFechaFinal());
		return periodosMensuales(p);
	}
	
	public static List<Periodo> periodosMensuales(final Periodo p){
		List<Periodo> periodos=new ArrayList<Periodo>();
		Set<Map<Integer,Integer>> meses=getMeses(p);
		for(Map<Integer,Integer> mes:meses){
			int year=mes.keySet().iterator().next();
			int month=mes.values().iterator().next();
			Periodo pp=getPeriodoEnUnMes(month,year);
			periodos.add(pp);
		}
		Collections.sort(periodos);
		if(periodos.size()==1){
			periodos.clear();
			periodos.add(p);
		}
		return periodos;
	}
	
	
	
	public static Set<Map<Integer,Integer>> getMeses(final Periodo p){
		
		Set<Map<Integer,Integer>> set=new TreeSet<Map<Integer,Integer>>(new Comparator(){

			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				Map<Integer,Integer> map1=(Map<Integer,Integer>)o1;
				Map<Integer,Integer> map2=(Map<Integer,Integer>)o2;
				Integer y1=map1.entrySet().iterator().next().getKey();
				Integer y2=map2.entrySet().iterator().next().getKey();
				if(y1.equals(y2)){
					Integer val1= map1.entrySet().iterator().next().getValue();
					Integer val2= map2.entrySet().iterator().next().getValue();
					return val1.compareTo(val2);
				}
				return y1.compareTo(y2);
			}
			
		});
		
		///Set<Map<Integer,Integer>> set=new HashSet<Map<Integer,Integer>>();
		//set=SetUtils.orderedSet(set);
		for(Date d:p.getListaDeDias()){
			Map<Integer,Integer> yearMonth=new HashMap<Integer,Integer>();
			int year=obtenerYear(d);
			int month=obtenerMes(d);
			yearMonth.put(year,month);
			//System.out.println("Analizando: Ao:"+year+"\t Mes: "+month);
			set.add(yearMonth);
		}
		
		return set;
	}
	
	public static int obtenerMes(Date d){
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		int mes=c.get(Calendar.MONTH);
		return mes;
	}
	
	public static int obtenerYear(Date d){
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		int year=c.get(Calendar.YEAR);
		return year;
	}

	public static Integer currentYear(){
		return obtenerYear(new Date())
	}

	public static Integer currentMes(){
		return obtenerMes(new Date())+1
	}

	public static Periodo ytd(){
		Calendar cal=Calendar.getInstance()
		cal.set(Calendar.MONTH,0)
		cal.set(Calendar.DATE,1)
		Date inicio=cal.getTime()
		Periodo p=new Periodo(fechaInicial:inicio,fechaFinal:new Date())
		return p;
		
	}
	

	static final List MESES=['ENERO','FEBRERO','MARZO','ABRIL','MAYO','JUNIO','JULIO','AGOSTO','SEPTIEMBRE','OCTUBRE','NOVIEMBRE','DICIEMBRE']
	static final List TRIMESTRES=['PRIMER','SEGUNDO','TERCERO','CUARTO']
	static final List CUATRIMESTRES=['PRIMER','SEGUNDO','TERCERO']
	static final List SEMESTRES=['PRIMER','SEGUNDO']
	

}
