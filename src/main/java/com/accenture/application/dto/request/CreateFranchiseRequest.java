package com.accenture.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear una nueva franquicia.
 *
 * @param name Nombre de la franquicia (requerido, máximo 255 caracteres)
 */
public record CreateFranchiseRequest(

        @NotBlank(message = "Franchise name must not be blank")
        @Size(max = 255, message = "Franchise name must not exceed 255 characters")
        String name

) {}
