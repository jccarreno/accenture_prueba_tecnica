package com.accenture.infrastructure.exception;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.shared.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Manejador global de errores para el stack reactivo WebFlux.
 *
 * <p>Implementa {@link ErrorWebExceptionHandler} en lugar de
 * {@code @ControllerAdvice} porque en WebFlux funcional los handlers no pasan
 * por el pipeline de Spring MVC, por lo que {@code @ExceptionHandler} no aplica.</p>
 *
 * <p>Mapeo de excepciones a códigos HTTP:</p>
 * <ul>
 *   <li>{@link ResourceNotFoundException}   → 404 Not Found</li>
 *   <li>{@link DuplicateResourceException}  → 409 Conflict</li>
 *   <li>{@link ValidationException}         → 400 Bad Request</li>
 *   <li>Cualquier otra excepción            → 500 Internal Server Error</li>
 * </ul>
 *
 * <p>{@code @Order(-2)} garantiza que este handler se ejecute antes del
 * {@code DefaultErrorWebExceptionHandler} de Spring Boot (que tiene orden -1).</p>
 */
@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Intercepta cualquier excepción del pipeline reactivo y escribe
     * una respuesta JSON uniforme usando {@link ApiResponse}.
     *
     * @param exchange intercambio HTTP activo
     * @param ex       excepción lanzada
     * @return {@code Mono<Void>} cuando la respuesta ha sido escrita
     */
    @Override
    public Mono<Void> handle( ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Unhandled exception: {}", ex.getMessage(), ex);
        } else {
            log.warn("Handled exception [{}]: {}", status, ex.getMessage());
        }

        ApiResponse<Void> body = ApiResponse.error(message);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            bytes = "{\"success\":false,\"message\":\"Internal serialization error\"}".getBytes();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Resuelve el {@link HttpStatus} apropiado según el tipo de excepción.
     *
     * @param ex excepción a clasificar
     * @return código HTTP correspondiente
     */
    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof DuplicateResourceException) return HttpStatus.CONFLICT;
        if (ex instanceof ValidationException)        return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}