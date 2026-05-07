package com.accenture.domain.port.out;

import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (repositorio) para persistencia de Franquicias.
 * La implementación concreta vive en la capa de infraestructura (R2DBC).
 * La capa de dominio depende solo de esta interfaz, no de la implementación.
 */
public interface FranchiseRepository {

    /**
     * Persiste una nueva franquicia.
     *
     * @param franchise Franquicia a guardar
     * @return Mono con la franquicia persistida (con ID generado)
     */
    Mono<Franchise> save(Franchise franchise);

    /**
     * Actualiza el nombre de una franquicia existente.
     *
     * @param id      ID de la franquicia
     * @param newName Nuevo nombre
     * @return Mono con la franquicia actualizada
     */
    Mono<Franchise> updateName(Long id, String newName);

    /**
     * Busca una franquicia por su ID.
     *
     * @param id ID a buscar
     * @return Mono con la franquicia, o Mono.empty() si no existe
     */
    Mono<Franchise> findById(Long id);

    /**
     * Retorna todas las franquicias almacenadas.
     *
     * @return Flux con todas las franquicias
     */
    Flux<Franchise> findAll();

    /**
     * Verifica si existe una franquicia con el nombre dado.
     *
     * @param name Nombre a verificar
     * @return Mono<Boolean> true si existe
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Retorna el producto de mayor stock por sucursal para una franquicia.
     *
     * @param franchiseId ID de la franquicia
     * @return Flux con la proyección TopStockProduct por cada sucursal
     */
    Flux<TopStockProduct> findTopStockProductsByFranchiseId(Long franchiseId);
}