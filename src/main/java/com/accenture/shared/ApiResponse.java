package com.accenture.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Envoltorio genérico de respuesta API para todas las respuestas REST.
 * Garantiza una estructura uniforme en toda la aplicación.
 *
 * @param <T>       Tipo del dato de respuesta
 * @param success   Indica si la operación fue exitosa
 * @param message   Mensaje descriptivo del resultado
 * @param data      Payload de datos (null en caso de error)
 * @param timestamp Fecha y hora de la respuesta
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    /**
     * Construye una respuesta exitosa con datos.
     *
     * @param message Mensaje de éxito
     * @param data    Payload a retornar
     * @return ApiResponse con success=true
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Construye una respuesta exitosa sin datos adicionales.
     *
     * @param message Mensaje de éxito
     * @return ApiResponse con success=true y data=null
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Construye una respuesta de error.
     *
     * @param message Mensaje descriptivo del error
     * @return ApiResponse con success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
