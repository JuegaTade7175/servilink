package com.example.demosass.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * RestTemplate configurado para llamadas a OpenStreetMap Nominatim.
     * Requiere User-Agent según política de uso de Nominatim.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Interceptor para añadir User-Agent (requerido por Nominatim OSM)
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "ServiLink/1.0 (DBP UTEC 2026; contact: servilink@utec.edu.pe)");
            request.getHeaders().set("Accept-Language", "es");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
