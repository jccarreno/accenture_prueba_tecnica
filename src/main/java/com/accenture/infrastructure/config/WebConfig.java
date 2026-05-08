package com.accenture.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de la capa Web.
 *
 * <p>Registra los beans de infraestructura transversal que Spring Boot WebFlux
 * no registra automáticamente cuando se trabaja con routers funcionales:</p>
 * <ul>
 *   <li>{@link Validator} de Jakarta — usado por {@code HandlerUtils} para validar DTOs.</li>
 *   <li>{@link ObjectMapper} de Jackson — usado por {@code GlobalErrorHandler} para
 *       serializar las respuestas de error a JSON.</li>
 * </ul>
 *
 * <p><strong>IMPORTANTE:</strong> No usar {@code @EnableWebFlux} en Spring Boot.
 * Esa anotación desactiva la autoconfiguración de Boot y rompe el registro de
 * {@code RouterFunction} beans, causando que todas las rutas devuelvan 404.</p>
 *
 * <p>En Spring Boot 4, {@code jackson-datatype-jsr310} (soporte para
 * {@code java.time.*}) está incluido en {@code spring-boot-starter-webflux}
 * y se autoconfigura — no es necesario registrar {@code JavaTimeModule}
 * manualmente.</p>
 */
@Configuration
public class WebConfig {

    /**
     * Crea y registra el {@link Validator} de Jakarta Validation.
     *
     * <p>Usa la implementación por defecto (Hibernate Validator) detectada
     * en el classpath. El factory se cierra automáticamente gracias al
     * bloque try-with-resources.</p>
     *
     * @return instancia de {@link Validator} lista para inyectar
     */
    @Bean
    public Validator validator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }

    /**
     * Crea y registra el {@link ObjectMapper} de Jackson.
     *
     * <p>{@code WRITE_DATES_AS_TIMESTAMPS = false} serializa los tipos
     * {@code java.time.*} como cadenas ISO-8601
     * (ej: {@code "2026-05-08T10:15:30"}) en lugar de arrays de epoch.
     * El soporte para {@code java.time} lo provee la autoconfiguración
     * de Spring Boot 4 a través de {@code JacksonAutoConfiguration}.</p>
     *
     * @return instancia de {@link ObjectMapper} lista para inyectar
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}