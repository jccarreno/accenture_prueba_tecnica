package com.accenture.infrastructure.adapter.out.persistence.mapper;

import com.accenture.domain.model.Franchise;
import com.accenture.infrastructure.adapter.out.persistence.entity.FranchiseEntity;

import java.util.List;

/**
 * Mapper estático para convertir entre {@link Franchise} (dominio)
 * y {@link FranchiseEntity} (persistencia).
 *
 * <p>Al ser métodos estáticos puros sin estado, no requiere ser un bean de Spring.</p>
 */
public final class FranchiseMapper {

    private FranchiseMapper() { }

    /**
     * Convierte una entidad R2DBC a modelo de dominio.
     * Las sucursales se inicializan vacías; se cargan por separado si se necesitan.
     *
     * @param entity entidad de persistencia
     * @return modelo de dominio
     */
    public static Franchise toDomain(FranchiseEntity entity) {
        return new Franchise(
                entity.getId(),
                entity.getName(),
                List.of()
        );
    }

    /**
     * Convierte un modelo de dominio a entidad de persistencia.
     * Los campos de auditoría ({@code createdAt}, {@code updatedAt}) los gestiona R2DBC.
     *
     * @param franchise modelo de dominio
     * @return entidad lista para persistir
     */
    public static FranchiseEntity toEntity(Franchise franchise) {
        return FranchiseEntity.builder()
                .id(franchise.id())
                .name(franchise.name())
                .build();
    }
}