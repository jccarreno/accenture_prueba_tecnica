package com.accenture.infrastructure.config;

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
 * </ul>
 *
 * <p><strong>¿Por qué no hay ObjectMapper aquí?</strong><br>
 * Spring Boot 4 autoconfigura un {@code ObjectMapper} (Jackson 3.x) con soporte
 * completo para {@code java.time.*} a través de {@code JacksonAutoConfiguration}.
 * Definir un bean manual con {@code new ObjectMapper()} crearía una instancia de
 * Jackson 2.x (diferente artefacto en el classpath) sin el módulo JSR-310,
 * causando {@code InvalidDefinitionException} al serializar {@link java.time.LocalDateTime}.
 * El bean autocconfigurado se inyecta directamente en {@code GlobalErrorHandler}.</p>
 *
 * <p><strong>IMPORTANTE:</strong> No usar {@code @EnableWebFlux} en Spring Boot.
 * Esa anotación desactiva la autoconfiguración de Boot y rompe el registro de
 * {@code RouterFunction} beans, causando que todas las rutas devuelvan 404.</p>
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
}