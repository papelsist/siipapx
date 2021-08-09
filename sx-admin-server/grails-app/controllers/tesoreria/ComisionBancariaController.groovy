package sx.tesoreria


import grails.rest.*
import grails.converters.*

class ComisionBancariaController extends RestfulController {
    static responseFormats = ['json', 'xml']
    ComisionBancariaController() {
        super(ComisionBancaria)
    }
}
