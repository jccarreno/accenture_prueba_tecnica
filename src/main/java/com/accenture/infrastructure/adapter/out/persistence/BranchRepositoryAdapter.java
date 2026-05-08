package com.accenture.infrastructure.adapter.out.persistence;

import com.accenture.domain.model.Branch;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.infrastructure.adapter.out.persistence.mapper.BranchMapper;
import com.accenture.infrastructure.adapter.out.persistence.repository.BranchR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida (puerto → infraestructura) para Sucursales.
 *
 * <p>Implementa el puerto de dominio {@link BranchRepository} delegando
 * en {@link BranchR2dbcRepository} (Spring Data R2DBC) y traduciendo
 * entre el modelo de dominio {@link Branch} y la entidad de persistencia
 * {@link com.accenture.infrastructure.adapter.out.persistence.entity.BranchEntity}
 * mediante {@link BranchMapper}.</p>
 *
 * <p>Al ser un {@code @Component}, Spring lo registra como bean y lo inyecta
 * automáticamente donde se requiera {@link BranchRepository}.</p>
 */
@Component
public class BranchRepositoryAdapter implements BranchRepository {

    private final BranchR2dbcRepository r2dbcRepository;

    public BranchRepositoryAdapter(BranchR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Convierte el dominio a entidad, persiste con R2DBC y mapea el resultado de vuelta.</p>
     */
    @Override
    public Mono<Branch> save(Branch branch) {
        return r2dbcRepository.save(BranchMapper.toEntity(branch))
                .map(BranchMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta el UPDATE parcial y luego busca la entidad actualizada para retornarla.</p>
     */
    @Override
    public Mono<Branch> updateName(Long id, String newName) {
        return r2dbcRepository.updateNameById(id, newName)
                .then(r2dbcRepository.findById(id))
                .map(BranchMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Branch> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(BranchMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Branch> findByFranchiseId(Long franchiseId) {
        return r2dbcRepository.findByFranchiseId(franchiseId)
                .map(BranchMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> existsByFranchiseIdAndName(Long franchiseId, String name) {
        return r2dbcRepository.existsByFranchiseIdAndName(franchiseId, name);
    }
}