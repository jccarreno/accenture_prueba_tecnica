package com.accenture.infrastructure.adapter.out.persistence.mapper;

import com.accenture.domain.model.Branch;
import com.accenture.infrastructure.adapter.out.persistence.entity.BranchEntity;

import java.util.List;

/**
 * Mapper estático para convertir entre {@link Branch} (dominio)
 * y {@link BranchEntity} (persistencia).
 */
public final class BranchMapper {

    private BranchMapper() { }

    /**
     * Convierte una entidad R2DBC a modelo de dominio.
     * Los productos se inicializan vacíos; se cargan por separado si se necesitan.
     *
     * @param entity entidad de persistencia
     * @return modelo de dominio
     */
    public static Branch toDomain(BranchEntity entity) {
        return new Branch(
                entity.getId(),
                entity.getFranchiseId(),
                entity.getName(),
                List.of()
        );
    }

    /**
     * Convierte un modelo de dominio a entidad de persistencia.
     *
     * @param branch modelo de dominio
     * @return entidad lista para persistir
     */
    public static BranchEntity toEntity(Branch branch) {
        return BranchEntity.builder()
                .id(branch.id())
                .franchiseId(branch.franchiseId())
                .name(branch.name())
                .build();
    }
}