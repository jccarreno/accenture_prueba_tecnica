package com.accenture.domain.port.in;

import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (use case) para operaciones sobre Franquicias.
 * Define el contrato que la capa de aplicación debe implementar.
 * Toda la API es reactiva usando Project Reactor (Mono/Flux).
 */
public interface FranchiseUseCase {

    /**
     * Crea una nueva franquicia en el sistema.
     *
     * @param franchise Datos de la franquicia a crear (sin ID)
     * @return Mono con la franquicia creada (con ID asignado)
     */
    Mono<Franchise> createFranchise(Franchise franchise);

    /**
     * Actualiza el nombre de una franquicia existente.
     *
     * @param id      ID de la franquicia a actualizar
     * @param newName Nuevo nombre para la franquicia
     * @return Mono con la franquicia actualizada
     */
    Mono<Franchise> updateFranchiseName(Long id, String newName);

    /**
     * Obtiene todas las franquicias registradas.
     *
     * @return Flux con todas las franquicias
     */
    Flux<Franchise> getAllFranchises();

    /**
     * Obtiene una franquicia por su ID.
     *
     * @param id ID de la franquicia a buscar
     * @return Mono con la franquicia encontrada, o error si no existe
     */
    Mono<Franchise> getFranchiseById(Long id);

    /**
     * Retorna el producto con mayor stock por sucursal para una franquicia dada.
     *
     * @param franchiseId ID de la franquicia a consultar
     * @return Flux con la proyección de top-stock por cada sucursal
     */
    Flux<TopStockProduct> getTopStockProductsByFranchise(Long franchiseId);
}
