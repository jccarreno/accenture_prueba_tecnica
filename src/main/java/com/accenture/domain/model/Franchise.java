package com.accenture.domain.model;

import java.util.List;

/**
 * Entidad de dominio que representa una Franquicia.
 * Modelo puro sin dependencias de infraestructura.
 *
 * @param id       Identificador único de la franquicia
 * @param name     Nombre de la franquicia
 * @param branches Lista de sucursales asociadas a la franquicia
 */
public record Franchise(
        Long id,
        String name,
        List<Branch> branches
) {

    /**
     * Crea una franquicia sin ID (para operaciones de creación).
     *
     * @param name     Nombre de la franquicia
     * @param branches Lista de sucursales
     * @return nueva instancia sin ID asignado
     */
    public static Franchise of(String name, List<Branch> branches) {
        return new Franchise(null, name, branches);
    }

    /**
     * Crea una franquicia sin sucursales (para creación inicial).
     *
     * @param name Nombre de la franquicia
     * @return nueva instancia sin sucursales ni ID
     */
    public static Franchise withName(String name) {
        return new Franchise(null, name, List.of());
    }

    /**
     * Retorna una copia de la franquicia con un nuevo nombre.
     *
     * @param newName Nuevo nombre para la franquicia
     * @return nueva instancia con el nombre actualizado
     */
    public Franchise withUpdatedName(String newName) {
        return new Franchise(this.id, newName, this.branches);
    }
}