package com.accenture.domain.port.in;

import com.accenture.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (use case) para operaciones sobre Sucursales.
 * Define el contrato que la capa de aplicación debe implementar.
 */
public interface BranchUseCase {

    /**
     * Agrega una nueva sucursal a una franquicia existente.
     *
     * @param branch Datos de la sucursal a crear (franchiseId debe estar presente)
     * @return Mono con la sucursal creada (con ID asignado)
     */
    Mono<Branch> addBranch(Branch branch);

    /**
     * Actualiza el nombre de una sucursal existente.
     *
     * @param id      ID de la sucursal a actualizar
     * @param newName Nuevo nombre para la sucursal
     * @return Mono con la sucursal actualizada
     */
    Mono<Branch> updateBranchName(Long id, String newName);

    /**
     * Obtiene todas las sucursales de una franquicia.
     *
     * @param franchiseId ID de la franquicia
     * @return Flux con todas las sucursales de la franquicia
     */
    Flux<Branch> getBranchesByFranchise(Long franchiseId);
}