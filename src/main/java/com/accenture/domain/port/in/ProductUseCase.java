package com.accenture.domain.port.in;

import com.accenture.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (use case) para operaciones sobre Productos.
 * Define el contrato que la capa de aplicación debe implementar.
 */
public interface ProductUseCase {

    /**
     * Agrega un nuevo producto a una sucursal existente.
     *
     * @param product Datos del producto a crear (branchId debe estar presente)
     * @return Mono con el producto creado (con ID asignado)
     */
    Mono<Product> addProduct(Product product);

    /**
     * Elimina un producto de una sucursal.
     *
     * @param productId ID del producto a eliminar
     * @return Mono vacío que señala la eliminación exitosa
     */
    Mono<Void> deleteProduct(Long productId);

    /**
     * Actualiza el stock de un producto existente.
     *
     * @param productId ID del producto a actualizar
     * @param newStock  Nuevo valor de stock
     * @return Mono con el producto actualizado
     */
    Mono<Product> updateStock(Long productId, Integer newStock);

    /**
     * Actualiza el nombre de un producto existente.
     *
     * @param productId ID del producto a actualizar
     * @param newName   Nuevo nombre para el producto
     * @return Mono con el producto actualizado
     */
    Mono<Product> updateProductName(Long productId, String newName);

    /**
     * Obtiene todos los productos de una sucursal.
     *
     * @param branchId ID de la sucursal
     * @return Flux con todos los productos de la sucursal
     */
    Flux<Product> getProductsByBranch(Long branchId);
}
