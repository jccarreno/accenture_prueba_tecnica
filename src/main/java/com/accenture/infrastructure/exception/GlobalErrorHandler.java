package com.accenture.infrastructure.exception;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.shared.ApiResponse;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
 *   <li>{@link DecodingException}           → 400 Bad Request (JSON malformado o tipo inválido)</li>
 *   <li>Cualquier otra excepción            → 500 Internal Server Error</li>
 * </ul>
 *
 * <p>{@code @Order(-2)} garantiza que este handler se ejecute antes del
 * {@code DefaultErrorWebExceptionHandler} de Spring Boot (que tiene orden -1).</p>
 *
 * <p><strong>Nota sobre Jackson 3.x:</strong> Spring Boot 4 migró a Jackson 3.x
 * ({@code tools.jackson.*}). El bean autoconfigrado es de tipo
 * {@code tools.jackson.databind.ObjectMapper}, no {@code com.fasterxml.jackson.databind.ObjectMapper}.
 * Usar el import incorrecto (2.x) causa {@code NoSuchBeanDefinitionException} al arrancar.</p>
 */
@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ObjectMapper objectMapper;

    /**
     * Inyecta el {@code ObjectMapper} autoconfigrado por Spring Boot 4 (Jackson 3.x).
     * Este bean ya incluye soporte para {@code java.time.*} (JSR-310).
     *
     * @param objectMapper bean de {@code tools.jackson.databind.ObjectMapper}
     */
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
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status  = resolveStatus(ex);
        String     message = resolveMessage(ex);

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Unhandled exception: {}", ex.getMessage(), ex);
        } else {
            log.warn("Handled exception [{}]: {}", status, message);
        }

        byte[] bytes = serializeError(message);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Serializa el mensaje de error a JSON usando el {@link ObjectMapper} de Jackson 3.x.
     * En caso de fallo inesperado cae a un JSON literal para garantizar respuesta válida.
     *
     * @param message mensaje de error
     * @return bytes UTF-8 del JSON resultante
     */
    private byte[] serializeError(String message) {
        try {
            ApiResponse<Void> body = ApiResponse.error(message);
            return objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            log.error("Failed to serialize error response, using fallback JSON", e);
            String timestamp  = LocalDateTime.now().format(ISO);
            String safeMsg    = message.replace("\"", "'");
            return ("{\"success\":false,\"message\":\"" + safeMsg + "\","
                    + "\"data\":null,\"timestamp\":\"" + timestamp + "\"}")
                    .getBytes();
        }
    }

    /**
     * Resuelve el {@link HttpStatus} apropiado según el tipo de excepción.
     *
     * @param ex excepción a clasificar
     * @return código HTTP correspondiente
     */
    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResourceNotFoundException)  return HttpStatus.NOT_FOUND;
        if (ex instanceof DuplicateResourceException) return HttpStatus.CONFLICT;
        if (ex instanceof ValidationException)        return HttpStatus.BAD_REQUEST;
        if (ex instanceof DecodingException)          return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Extrae un mensaje legible para el cliente según el tipo de excepción.
     *
     * <p>Para {@link DecodingException}, la causa raíz de Jackson tiene el
     * mensaje más preciso (ej: "Cannot deserialize value of type Integer from String").</p>
     *
     * @param ex excepción lanzada
     * @return mensaje a incluir en la respuesta de error
     */
    private String resolveMessage(Throwable ex) {
        if (ex instanceof DecodingException) {
            Throwable cause = ex.getCause();
            return (cause != null && cause.getMessage() != null)
                    ? cause.getMessage()
                    : "Invalid request body: check field types and values";
        }
        return ex.getMessage() != null ? ex.getMessage() : "Unexpected error";
    }
}