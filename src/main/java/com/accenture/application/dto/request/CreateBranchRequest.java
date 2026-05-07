package com.accenture.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para agregar una nueva sucursal a una franquicia.
 *
 * @param franchiseId ID de la franquicia a la que pertenece la sucursal
 * @param name        Nombre de la sucursal
 */
public record CreateBranchRequest(

        @NotNull(message = "Franchise ID must not be null")
        @Positive(message = "Franchise ID must be a positive number")
        Long franchiseId,

        @NotBlank(message = "Branch name must not be blank")
        @Size(max = 255, message = "Branch name must not exceed 255 characters")
        String name

) {}
