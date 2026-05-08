package com.accenture.infrastructure.adapter.in.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilidades transversales para los handlers funcionales de WebFlux.
 *
 * <p>Centraliza la validación de DTOs mediante el {@link Validator} de Jakarta,
 * evitando código duplicado entre handlers. Al ser un {@code @Component},
 * se inyecta como dependencia estática después del arranque.</p>
 */
@Component
public class HandlerUtils {

    private static Validator validator;

    /**
     * Inyección del {@link Validator} de Jakarta vía setter post-construcción.
     * Se almacena como estático para poder usarse en métodos de utilidad estáticos.
     *
     * @param v instancia de Validator provista por Spring
     */
    @Autowired
    public void setValidator(Validator v) {
        HandlerUtils.validator = v;
    }

    /**
     * Valida un DTO usando las anotaciones de Jakarta Validation.
     *
     * <p>Si hay violaciones, emite un {@link ValidationException} con todos los mensajes
     * concatenados, separados por "; ". El error es capturado por el
     * {@code GlobalErrorHandler} y mapeado a HTTP 400.</p>
     *
     * @param dto objeto a validar
     * @param <T> tipo del DTO
     * @return {@code Mono<T>} con el DTO si es válido, o {@code Mono.error} si no lo es
     */
    public static <T> Mono<T> validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return Mono.error(new ValidationException(msg));
        }
        return Mono.just(dto);
    }
}