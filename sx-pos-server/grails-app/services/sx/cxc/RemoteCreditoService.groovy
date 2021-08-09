package sx.cxc

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import sx.core.Cliente

class RemoteCreditoService {

    RestTemplateBuilder restTemplateBuilder

    final RestTemplate restTemplate

    RemoteCreditoService() {
        // restTemplate = restTemplateBuilder.build();
    }



    def actualizarCredito(){
        // this.restTemplate.getForObject("api/clientes/${cliente.id}", Cliente.class)
    }
}
