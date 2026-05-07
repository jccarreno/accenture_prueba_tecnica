package com.accenture.domain.model;

/**
 * Entidad de dominio que representa un Producto ofertado en una Sucursal.
 * Modelo puro sin dependencias de infraestructura.
 *
 * @param id       Identificador único del producto
 * @param branchId ID de la sucursal a la que pertenece
 * @param name     Nombre del producto
 * @param stock    Cantidad de stock disponible
 */
public record Product(
        Long id,
        Long branchId,
        String name,
        Integer stock
) {

    /**
     * Crea un producto sin ID (para operaciones de creación).
     *
     * @param branchId ID de la sucursal padre
     * @param name     Nombre del producto
     * @param stock    Stock inicial
     * @return nueva instancia sin ID asignado
     */
    public static Product of(Long branchId, String name, Integer stock) {
        return new Product(null, branchId, name, stock);
    }

    /**
     * Retorna una copia del producto con el stock actualizado.
     *
     * @param newStock Nuevo valor de stock
     * @return nueva instancia con el stock actualizado
     */
    public Product withUpdatedStock(Integer newStock) {
        return new Product(this.id, this.branchId, this.name, newStock);
    }

    /**
     * Retorna una copia del producto con el nombre actualizado.
     *
     * @param newName Nuevo nombre del producto
     * @return nueva instancia con el nombre actualizado
     */
    public Product withUpdatedName(String newName) {
        return new Product(this.id, this.branchId, newName, this.stock);
    }
}