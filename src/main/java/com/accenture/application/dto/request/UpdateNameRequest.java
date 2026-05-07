package com.accenture.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada genérico para actualizar el nombre de un recurso.
 * Reutilizado por franquicias, sucursales y productos.
 *
 * @param name Nuevo nombre del recurso
 */
public record UpdateNameRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name

) {}