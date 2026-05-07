package com.accenture.domain.model;

import java.util.List;

/**
 * Entidad de dominio que representa una Sucursal perteneciente a una Franquicia.
 * Modelo puro sin dependencias de infraestructura.
 *
 * @param id           Identificador único de la sucursal
 * @param franchiseId  ID de la franquicia a la que pertenece
 * @param name         Nombre de la sucursal
 * @param products     Lista de productos ofertados en la sucursal
 */
public record Branch(
        Long id,
        Long franchiseId,
        String name,
        List<Product> products
) {

    /**
     * Crea una sucursal sin ID (para operaciones de creación).
     *
     * @param franchiseId ID de la franquicia padre
     * @param name        Nombre de la sucursal
     * @return nueva instancia sin ID asignado
     */
    public static Branch of(Long franchiseId, String name) {
        return new Branch(null, franchiseId, name, List.of());
    }

    /**
     * Retorna una copia de la sucursal con un nuevo nombre.
     *
     * @param newName Nuevo nombre para la sucursal
     * @return nueva instancia con el nombre actualizado
     */
    public Branch withUpdatedName(String newName) {
        return new Branch(this.id, this.franchiseId, newName, this.products);
    }
}