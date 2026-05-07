package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Branch;
import com.accenture.domain.port.in.BranchUseCase;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.FranchiseRepository;
import com.accenture.shared.ApiConstants;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación de los casos de uso de Sucursal.
 *
 * <p>Orquesta validaciones de negocio: verifica que la franquicia padre
 * exista y que no haya duplicidad de nombres dentro de la misma franquicia.</p>
 */
@Service
public class BranchService implements BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public BranchService(BranchRepository branchRepository,
                         FranchiseRepository franchiseRepository) {
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que la franquicia exista y que el nombre sea único dentro de ella.</p>
     */
    @Override
    public Mono<Branch> addBranch(Branch branch) {
        return franchiseRepository.findById(branch.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_FRANCHISE, branch.franchiseId())))
                .flatMap(franchise ->
                        branchRepository.existsByFranchiseIdAndName(branch.franchiseId(), branch.name()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException(
                                ApiConstants.RESOURCE_BRANCH, "name", branch.name()));
                    }
                    return branchRepository.save(branch);
                });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que la sucursal exista y que el nuevo nombre no colisione
     * con otra sucursal de la misma franquicia.</p>
     */
    @Override
    public Mono<Branch> updateBranchName(Long id, String newName) {
        return branchRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_BRANCH, id)))
                .flatMap(existing ->
                        branchRepository.existsByFranchiseIdAndName(existing.franchiseId(), newName)
                                .flatMap(nameExists -> {
                                    if (Boolean.TRUE.equals(nameExists)) {
                                        return Mono.error(new DuplicateResourceException(
                                                ApiConstants.RESOURCE_BRANCH, "name", newName));
                                    }
                                    return branchRepository.updateName(id, newName);
                                }));
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Branch> getBranchesByFranchise(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_FRANCHISE, franchiseId)))
                .thenMany(branchRepository.findByFranchiseId(franchiseId));
    }
}
