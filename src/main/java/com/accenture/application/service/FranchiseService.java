package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.TopStockProduct;
import com.accenture.domain.port.in.FranchiseUseCase;
import com.accenture.domain.port.out.FranchiseRepository;
import com.accenture.shared.ApiConstants;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación de los casos de uso de Franquicia.
 *
 * <p>Esta clase pertenece a la capa de aplicación: orquesta la lógica
 * de negocio usando los puertos de dominio, sin conocer detalles
 * de infraestructura (HTTP, R2DBC, etc.).</p>
 *
 * <h3>Nota sobre operadores reactivos</h3>
 * <p>Se usa {@code flatMapMany} en lugar de {@code thenMany} para encadenar
 * el {@code Flux} de resultados. {@code thenMany} descarta el elemento upstream
 * y, por tanto, también ignora cualquier error emitido por {@code switchIfEmpty},
 * haciendo que el {@code ResourceNotFoundException} nunca llegue al suscriptor.
 * {@code flatMapMany} propaga tanto el valor como el error correctamente.</p>
 */
@Service
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public FranchiseService(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica unicidad del nombre antes de persistir.</p>
     */
    @Override
    public Mono<Franchise> createFranchise(Franchise franchise) {
        return franchiseRepository.existsByName(franchise.name())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException(
                                ApiConstants.RESOURCE_FRANCHISE, "name", franchise.name()));
                    }
                    return franchiseRepository.save(franchise);
                });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que la franquicia exista y que el nuevo nombre no esté tomado.</p>
     */
    @Override
    public Mono<Franchise> updateFranchiseName(Long id, String newName) {
        return franchiseRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_FRANCHISE, id)))
                .flatMap(existing -> franchiseRepository.existsByName(newName))
                .flatMap(nameExists -> {
                    if (Boolean.TRUE.equals(nameExists)) {
                        return Mono.error(new DuplicateResourceException(
                                ApiConstants.RESOURCE_FRANCHISE, "name", newName));
                    }
                    return franchiseRepository.updateName(id, newName);
                });
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Franchise> getAllFranchises() {
        return franchiseRepository.findAll();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lanza {@link ResourceNotFoundException} si no existe.</p>
     */
    @Override
    public Mono<Franchise> getFranchiseById(Long id) {
        return franchiseRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_FRANCHISE, id)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que la franquicia exista antes de consultar el top stock.
     * Usa {@code flatMapMany} en lugar de {@code thenMany} para que el error
     * de {@code switchIfEmpty} se propague correctamente al suscriptor.</p>
     */
    @Override
    public Flux<TopStockProduct> getTopStockProductsByFranchise(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_FRANCHISE, franchiseId)))
                .flatMapMany(franchise ->
                        franchiseRepository.findTopStockProductsByFranchiseId(franchiseId));
    }
}