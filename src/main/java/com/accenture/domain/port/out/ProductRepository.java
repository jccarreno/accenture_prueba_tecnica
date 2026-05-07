package com.accenture.domain.port.out;

import com.accenture.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (repositorio) para persistencia de Productos.
 * La implementación concreta vive en la capa de infraestructura (R2DBC).
 */
public interface ProductRepository {

    /**
     * Persiste un nuevo producto.
     *
     * @param product Producto a guardar
     * @return Mono con el producto persistido (con ID generado)
     */
    Mono<Product> save(Product product);

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar
     * @return Mono vacío al completar
     */
    Mono<Void> deleteById(Long id);

    /**
     * Actualiza el stock de un producto.
     *
     * @param id       ID del producto
     * @param newStock Nuevo valor de stock
     * @return Mono con el producto actualizado
     */
    Mono<Product> updateStock(Long id, Integer newStock);

    /**
     * Actualiza el nombre de un producto.
     *
     * @param id      ID del producto
     * @param newName Nuevo nombre
     * @return Mono con el producto actualizado
     */
    Mono<Product> updateName(Long id, String newName);

    /**
     * Busca un producto por su ID.
     *
     * @param id ID a buscar
     * @return Mono con el producto, o Mono.empty() si no existe
     */
    Mono<Product> findById(Long id);

    /**
     * Retorna todos los productos de una sucursal.
     *
     * @param branchId ID de la sucursal padre
     * @return Flux con todos los productos
     */
    Flux<Product> findByBranchId(Long branchId);

    /**
     * Verifica si existe un producto con el nombre dado dentro de una sucursal.
     *
     * @param branchId ID de la sucursal
     * @param name     Nombre a verificar
     * @return Mono<Boolean> true si existe
     */
    Mono<Boolean> existsByBranchIdAndName(Long branchId, String name);
}
