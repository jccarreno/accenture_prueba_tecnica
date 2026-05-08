package com.accenture.infrastructure.adapter.out.persistence.repository;

import com.accenture.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio técnico R2DBC para {@link ProductEntity}.
 * Extiende {@link ReactiveCrudRepository} para operaciones CRUD básicas
 * y declara queries personalizadas necesarias para los casos de uso.
 */
public interface ProductR2dbcRepository extends ReactiveCrudRepository<ProductEntity, Long> {

    /**
     * Retorna todos los productos pertenecientes a una sucursal.
     *
     * @param branchId ID de la sucursal
     * @return Flux con los productos encontrados
     */
    Flux<ProductEntity> findByBranchId(Long branchId);

    /**
     * Verifica si ya existe un producto con el mismo nombre dentro de una sucursal.
     *
     * @param branchId ID de la sucursal
     * @param name     nombre a verificar
     * @return {@code true} si ya existe
     */
    Mono<Boolean> existsByBranchIdAndName(Long branchId, String name);

    /**
     * Actualiza el stock de un producto.
     *
     * @param id       ID del producto
     * @param newStock nuevo valor de stock
     * @return número de filas afectadas
     */
    @Modifying
    @Query("UPDATE product SET stock = :newStock WHERE id = :id")
    Mono<Integer> updateStockById(Long id, Integer newStock);

    /**
     * Actualiza el nombre de un producto.
     *
     * @param id      ID del producto
     * @param newName nuevo nombre
     * @return número de filas afectadas
     */
    @Modifying
    @Query("UPDATE product SET name = :newName WHERE id = :id")
    Mono<Integer> updateNameById(Long id, String newName);
}