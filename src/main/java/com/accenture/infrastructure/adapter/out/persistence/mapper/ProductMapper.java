package com.accenture.infrastructure.adapter.out.persistence.mapper;

import com.accenture.domain.model.Product;
import com.accenture.infrastructure.adapter.out.persistence.entity.ProductEntity;

/**
 * Mapper estático para convertir entre {@link Product} (dominio)
 * y {@link ProductEntity} (persistencia).
 */
public final class ProductMapper {

    private ProductMapper() { }

    /**
     * Convierte una entidad R2DBC a modelo de dominio.
     *
     * @param entity entidad de persistencia
     * @return modelo de dominio
     */
    public static Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getBranchId(),
                entity.getName(),
                entity.getStock()
        );
    }

    /**
     * Convierte un modelo de dominio a entidad de persistencia.
     *
     * @param product modelo de dominio
     * @return entidad lista para persistir
     */
    public static ProductEntity toEntity(Product product) {
        return ProductEntity.builder()
                .id(product.id())
                .branchId(product.branchId())
                .name(product.name())
                .stock(product.stock())
                .build();
    }
}