package com.accenture.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para agregar un nuevo producto a una sucursal.
 *
 * @param branchId ID de la sucursal a la que pertenece el producto
 * @param name     Nombre del producto
 * @param stock    Stock inicial del producto (mínimo 0)
 */
public record CreateProductRequest(

        @NotNull(message = "Branch ID must not be null")
        @Positive(message = "Branch ID must be a positive number")
        Long branchId,

        @NotBlank(message = "Product name must not be blank")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @NotNull(message = "Stock must not be null")
        @Min(value = 0, message = "Stock must be zero or greater")
        Integer stock

) {}
