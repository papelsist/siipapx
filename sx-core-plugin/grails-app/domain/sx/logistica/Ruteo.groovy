package sx.logistica

import sx.core.Sucursal

class Ruteo {

    String	id

    String	d_codigo

    String	id_asentamiento_cp

    String	d_asentamiento

    String	d_municipio

    String	d_ciudad

    String	d_estado

    String	d_tipo_asentamiento

    String	d_cp

    String	c_estado

    String	c_oficina

    String	c_cp

    String	c_tipo_asentamiento

    String	c_municipio

    String	d_zona

    String	c_cve_ciudad

    Sucursal suc_asignada

    static constraints = {
        suc_asignada nullable:true
    }

    static mapping = {
        id generator: 'uuid'
    }

}
