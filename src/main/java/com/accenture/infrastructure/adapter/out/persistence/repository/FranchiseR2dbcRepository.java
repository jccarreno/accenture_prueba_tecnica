package com.accenture.infrastructure.adapter.out.persistence.repository;

import com.accenture.infrastructure.adapter.out.persistence.entity.FranchiseEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio técnico R2DBC para {@link FranchiseEntity}.
 * Extiende {@link ReactiveCrudRepository} para operaciones CRUD básicas
 * y declara queries personalizadas necesarias para los casos de uso.
 */
public interface FranchiseR2dbcRepository extends ReactiveCrudRepository<FranchiseEntity, Long> {

    /**
     * Verifica si existe una franquicia con el nombre dado (case-sensitive).
     *
     * @param name nombre a verificar
     * @return {@code true} si ya existe
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Actualiza solo el nombre de una franquicia, preservando {@code updated_at}
     * automáticamente por la columna {@code ON UPDATE CURRENT_TIMESTAMP} del DDL.
     *
     * @param id      ID de la franquicia
     * @param newName nuevo nombre
     * @return número de filas afectadas
     */
    @Modifying
    @Query("UPDATE franchise SET name = :newName WHERE id = :id")
    Mono<Integer> updateNameById(Long id, String newName);
}