package com.accenture.domain.model;

/**
 * Proyección de dominio que representa el producto con mayor stock
 * por sucursal para una franquicia puntual.
 * Utilizada como resultado del caso de uso de consulta de top stock.
 *
 * @param branchId   ID de la sucursal
 * @param branchName Nombre de la sucursal
 * @param productId  ID del producto con mayor stock
 * @param productName Nombre del producto con mayor stock
 * @param stock      Cantidad de stock del producto
 */
public record TopStockProduct(
        Long branchId,
        String branchName,
        Long productId,
        String productName,
        Integer stock
) {}