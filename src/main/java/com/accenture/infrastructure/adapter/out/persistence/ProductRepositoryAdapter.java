package com.accenture.infrastructure.adapter.out.persistence;

import com.accenture.domain.model.Product;
import com.accenture.domain.port.out.ProductRepository;
import com.accenture.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.accenture.infrastructure.adapter.out.persistence.repository.ProductR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida (puerto → infraestructura) para Productos.
 *
 * <p>Implementa el puerto de dominio {@link ProductRepository} delegando
 * en {@link ProductR2dbcRepository} (Spring Data R2DBC) y traduciendo
 * entre el modelo de dominio {@link Product} y la entidad de persistencia
 * {@link com.accenture.infrastructure.adapter.out.persistence.entity.ProductEntity}
 * mediante {@link ProductMapper}.</p>
 */
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductR2dbcRepository r2dbcRepository;

    public ProductRepositoryAdapter(ProductR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Convierte el dominio a entidad, persiste con R2DBC y mapea el resultado de vuelta.</p>
     */
    @Override
    public Mono<Product> save(Product product) {
        return r2dbcRepository.save(ProductMapper.toEntity(product))
                .map(ProductMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delega directamente en el método {@code deleteById} heredado de
     * {@code ReactiveCrudRepository}.</p>
     */
    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta el UPDATE parcial de stock y luego busca el producto actualizado.</p>
     */
    @Override
    public Mono<Product> updateStock(Long id, Integer newStock) {
        return r2dbcRepository.updateStockById(id, newStock)
                .then(r2dbcRepository.findById(id))
                .map(ProductMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta el UPDATE parcial de nombre y luego busca el producto actualizado.</p>
     */
    @Override
    public Mono<Product> updateName(Long id, String newName) {
        return r2dbcRepository.updateNameById(id, newName)
                .then(r2dbcRepository.findById(id))
                .map(ProductMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Product> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(ProductMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Product> findByBranchId(Long branchId) {
        return r2dbcRepository.findByBranchId(branchId)
                .map(ProductMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> existsByBranchIdAndName(Long branchId, String name) {
        return r2dbcRepository.existsByBranchIdAndName(branchId, name);
    }
}