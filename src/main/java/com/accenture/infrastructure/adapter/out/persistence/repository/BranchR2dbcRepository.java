package com.accenture.infrastructure.adapter.out.persistence.repository;

import com.accenture.infrastructure.adapter.out.persistence.entity.BranchEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio técnico R2DBC para {@link BranchEntity}.
 * Extiende {@link ReactiveCrudRepository} para operaciones CRUD básicas
 * y declara queries personalizadas necesarias para los casos de uso.
 */
public interface BranchR2dbcRepository extends ReactiveCrudRepository<BranchEntity, Long> {

    /**
     * Retorna todas las sucursales pertenecientes a una franquicia.
     *
     * @param franchiseId ID de la franquicia
     * @return Flux con las sucursales encontradas
     */
    Flux<BranchEntity> findByFranchiseId(Long franchiseId);

    /**
     * Verifica si ya existe una sucursal con el mismo nombre dentro de una franquicia.
     *
     * @param franchiseId ID de la franquicia
     * @param name        nombre a verificar
     * @return {@code true} si ya existe
     */
    Mono<Boolean> existsByFranchiseIdAndName(Long franchiseId, String name);

    /**
     * Actualiza solo el nombre de una sucursal.
     *
     * @param id      ID de la sucursal
     * @param newName nuevo nombre
     * @return número de filas afectadas
     */
    @Modifying
    @Query("UPDATE branch SET name = :newName WHERE id = :id")
    Mono<Integer> updateNameById(Long id, String newName);
}