package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Branch;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.FranchiseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link BranchService}.
 *
 * <p>Verifica las reglas de negocio: existencia de la franquicia padre,
 * unicidad de nombre dentro de la franquicia y existencia de la sucursal
 * antes de modificarla.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BranchService — Unit Tests")
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private BranchService branchService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final Long   FRANCHISE_ID  = 1L;
    private static final Long   BRANCH_ID     = 10L;
    private static final String BRANCH_NAME   = "Sucursal Bogotá Centro";

    private Franchise buildFranchise() {
        return new Franchise(FRANCHISE_ID, "McDonald's Colombia", List.of());
    }

    private Branch buildBranch() {
        return new Branch(BRANCH_ID, FRANCHISE_ID, BRANCH_NAME, List.of());
    }

    // ── addBranch ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addBranch")
    class AddBranch {

        @Test
        @DisplayName("debe agregar la sucursal cuando la franquicia existe y el nombre es único")
        void shouldAddBranch_whenFranchiseExistsAndNameIsUnique() {
            Branch input = Branch.of(FRANCHISE_ID, BRANCH_NAME);
            Branch saved = buildBranch();

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(branchRepository.existsByFranchiseIdAndName(FRANCHISE_ID, BRANCH_NAME))
                    .thenReturn(Mono.just(false));
            when(branchRepository.save(any())).thenReturn(Mono.just(saved));

            StepVerifier.create(branchService.addBranch(input))
                    .expectNextMatches(b ->
                            b.id().equals(BRANCH_ID) &&
                            b.franchiseId().equals(FRANCHISE_ID) &&
                            b.name().equals(BRANCH_NAME))
                    .verifyComplete();

            verify(franchiseRepository).findById(FRANCHISE_ID);
            verify(branchRepository).existsByFranchiseIdAndName(FRANCHISE_ID, BRANCH_NAME);
            verify(branchRepository).save(any());
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la franquicia no existe")
        void shouldThrowNotFound_whenFranchiseDoesNotExist() {
            Branch input = Branch.of(FRANCHISE_ID, BRANCH_NAME);

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(branchService.addBranch(input))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(branchRepository, never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nombre ya existe en la franquicia")
        void shouldThrowDuplicate_whenNameAlreadyExistsInFranchise() {
            Branch input = Branch.of(FRANCHISE_ID, BRANCH_NAME);

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(branchRepository.existsByFranchiseIdAndName(FRANCHISE_ID, BRANCH_NAME))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(branchService.addBranch(input))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(branchRepository, never()).save(any());
        }
    }

    // ── updateBranchName ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateBranchName")
    class UpdateBranchName {

        @Test
        @DisplayName("debe actualizar el nombre cuando la sucursal existe y el nuevo nombre es único")
        void shouldUpdateName_whenBranchExistsAndNameIsUnique() {
            String newName   = "Sucursal Bogotá Norte";
            Branch updated   = new Branch(BRANCH_ID, FRANCHISE_ID, newName, List.of());

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(branchRepository.existsByFranchiseIdAndName(FRANCHISE_ID, newName))
                    .thenReturn(Mono.just(false));
            when(branchRepository.updateName(BRANCH_ID, newName)).thenReturn(Mono.just(updated));

            StepVerifier.create(branchService.updateBranchName(BRANCH_ID, newName))
                    .expectNextMatches(b -> b.name().equals(newName))
                    .verifyComplete();

            verify(branchRepository).findById(BRANCH_ID);
            verify(branchRepository).existsByFranchiseIdAndName(FRANCHISE_ID, newName);
            verify(branchRepository).updateName(BRANCH_ID, newName);
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la sucursal no existe")
        void shouldThrowNotFound_whenBranchDoesNotExist() {
            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.empty());

            StepVerifier.create(branchService.updateBranchName(BRANCH_ID, "Nuevo"))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(branchRepository, never()).updateName(any(), any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nuevo nombre ya existe en la franquicia")
        void shouldThrowDuplicate_whenNewNameAlreadyTakenInFranchise() {
            String newName = "Sucursal Medellín El Poblado";

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(branchRepository.existsByFranchiseIdAndName(FRANCHISE_ID, newName))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(branchService.updateBranchName(BRANCH_ID, newName))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(branchRepository, never()).updateName(any(), any());
        }
    }

    // ── getBranchesByFranchise ────────────────────────────────────────────────

    @Nested
    @DisplayName("getBranchesByFranchise")
    class GetBranchesByFranchise {

        @Test
        @DisplayName("debe retornar las sucursales cuando la franquicia existe")
        void shouldReturnBranches_whenFranchiseExists() {
            Branch b1 = buildBranch();
            Branch b2 = new Branch(11L, FRANCHISE_ID, "Sucursal Medellín", List.of());

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(b1, b2));

            StepVerifier.create(branchService.getBranchesByFranchise(FRANCHISE_ID))
                    .expectNext(b1)
                    .expectNext(b2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando la franquicia existe pero no tiene sucursales")
        void shouldReturnEmpty_whenFranchiseHasNoBranches() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(branchRepository.findByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

            StepVerifier.create(branchService.getBranchesByFranchise(FRANCHISE_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la franquicia no existe")
        void shouldThrowNotFound_whenFranchiseDoesNotExist() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(branchService.getBranchesByFranchise(FRANCHISE_ID))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(branchRepository, never()).findByFranchiseId(any());
        }
    }
}