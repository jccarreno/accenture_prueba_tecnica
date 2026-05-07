package com.accenture.domain.port.out;

import com.accenture.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (repositorio) para persistencia de Sucursales.
 * La implementación concreta vive en la capa de infraestructura (R2DBC).
 */
public interface BranchRepository {

    /**
     * Persiste una nueva sucursal.
     *
     * @param branch Sucursal a guardar
     * @return Mono con la sucursal persistida (con ID generado)
     */
    Mono<Branch> save(Branch branch);

    /**
     * Actualiza el nombre de una sucursal existente.
     *
     * @param id      ID de la sucursal
     * @param newName Nuevo nombre
     * @return Mono con la sucursal actualizada
     */
    Mono<Branch> updateName(Long id, String newName);

    /**
     * Busca una sucursal por su ID.
     *
     * @param id ID a buscar
     * @return Mono con la sucursal, o Mono.empty() si no existe
     */
    Mono<Branch> findById(Long id);

    /**
     * Retorna todas las sucursales de una franquicia.
     *
     * @param franchiseId ID de la franquicia padre
     * @return Flux con todas las sucursales
     */
    Flux<Branch> findByFranchiseId(Long franchiseId);

    /**
     * Verifica si existe una sucursal con el nombre dado dentro de una franquicia.
     *
     * @param franchiseId ID de la franquicia
     * @param name        Nombre a verificar
     * @return Mono<Boolean> true si existe
     */
    Mono<Boolean> existsByFranchiseIdAndName(Long franchiseId, String name);
}