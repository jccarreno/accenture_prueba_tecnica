package com.accenture.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para actualizar el stock de un producto.
 *
 * @param stock Nuevo valor de stock (mínimo 0)
 */
public record UpdateStockRequest(

        @NotNull(message = "Stock must not be null")
        @Min(value = 0, message = "Stock must be zero or greater")
        Integer stock

) {}