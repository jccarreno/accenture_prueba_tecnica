package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.TopStockProduct;
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
 * Pruebas unitarias para {@link FranchiseService}.
 *
 * <p>Valida la lógica de negocio de la capa de aplicación de forma aislada,
 * mockeando el puerto de salida {@link FranchiseRepository}.
 * Se usa {@link StepVerifier} para verificar flujos reactivos (Mono/Flux).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FranchiseService — Unit Tests")
class FranchiseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseService franchiseService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final Long   FRANCHISE_ID   = 1L;
    private static final String FRANCHISE_NAME = "McDonald's Colombia";

    private Franchise buildFranchise() {
        return new Franchise(FRANCHISE_ID, FRANCHISE_NAME, List.of());
    }

    // ── createFranchise ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("createFranchise")
    class CreateFranchise {

        @Test
        @DisplayName("debe crear la franquicia cuando el nombre no existe")
        void shouldCreateFranchise_whenNameIsUnique() {
            Franchise input   = Franchise.withName(FRANCHISE_NAME);
            Franchise saved   = buildFranchise();

            when(franchiseRepository.existsByName(FRANCHISE_NAME)).thenReturn(Mono.just(false));
            when(franchiseRepository.save(any())).thenReturn(Mono.just(saved));

            StepVerifier.create(franchiseService.createFranchise(input))
                    .expectNextMatches(f ->
                            f.id().equals(FRANCHISE_ID) &&
                            f.name().equals(FRANCHISE_NAME))
                    .verifyComplete();

            verify(franchiseRepository).existsByName(FRANCHISE_NAME);
            verify(franchiseRepository).save(any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nombre ya existe")
        void shouldThrowDuplicate_whenNameAlreadyExists() {
            Franchise input = Franchise.withName(FRANCHISE_NAME);

            when(franchiseRepository.existsByName(FRANCHISE_NAME)).thenReturn(Mono.just(true));

            StepVerifier.create(franchiseService.createFranchise(input))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(franchiseRepository).existsByName(FRANCHISE_NAME);
            verify(franchiseRepository, never()).save(any());
        }
    }

    // ── updateFranchiseName ───────────────────────────────────────────────────

    @Nested
    @DisplayName("updateFranchiseName")
    class UpdateFranchiseName {

        @Test
        @DisplayName("debe actualizar el nombre cuando la franquicia existe y el nombre es único")
        void shouldUpdateName_whenFranchiseExistsAndNameIsUnique() {
            String newName  = "McDonald's Nuevo";
            Franchise updated = new Franchise(FRANCHISE_ID, newName, List.of());

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(false));
            when(franchiseRepository.updateName(FRANCHISE_ID, newName)).thenReturn(Mono.just(updated));

            StepVerifier.create(franchiseService.updateFranchiseName(FRANCHISE_ID, newName))
                    .expectNextMatches(f -> f.name().equals(newName))
                    .verifyComplete();

            verify(franchiseRepository).findById(FRANCHISE_ID);
            verify(franchiseRepository).existsByName(newName);
            verify(franchiseRepository).updateName(FRANCHISE_ID, newName);
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la franquicia no existe")
        void shouldThrowNotFound_whenFranchiseDoesNotExist() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(franchiseService.updateFranchiseName(FRANCHISE_ID, "Nuevo"))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(franchiseRepository).findById(FRANCHISE_ID);
            verify(franchiseRepository, never()).updateName(any(), any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nuevo nombre ya está en uso")
        void shouldThrowDuplicate_whenNewNameAlreadyTaken() {
            String newName = "Burger King Colombia";

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(true));

            StepVerifier.create(franchiseService.updateFranchiseName(FRANCHISE_ID, newName))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(franchiseRepository).existsByName(newName);
            verify(franchiseRepository, never()).updateName(any(), any());
        }
    }

    // ── getAllFranchises ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllFranchises")
    class GetAllFranchises {

        @Test
        @DisplayName("debe retornar todas las franquicias registradas")
        void shouldReturnAllFranchises() {
            Franchise f1 = buildFranchise();
            Franchise f2 = new Franchise(2L, "Burger King", List.of());

            when(franchiseRepository.findAll()).thenReturn(Flux.just(f1, f2));

            StepVerifier.create(franchiseService.getAllFranchises())
                    .expectNext(f1)
                    .expectNext(f2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando no hay franquicias")
        void shouldReturnEmpty_whenNoFranchisesExist() {
            when(franchiseRepository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(franchiseService.getAllFranchises())
                    .verifyComplete();
        }
    }

    // ── getFranchiseById ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getFranchiseById")
    class GetFranchiseById {

        @Test
        @DisplayName("debe retornar la franquicia cuando existe el ID")
        void shouldReturnFranchise_whenIdExists() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));

            StepVerifier.create(franchiseService.getFranchiseById(FRANCHISE_ID))
                    .expectNextMatches(f -> f.id().equals(FRANCHISE_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando el ID no existe")
        void shouldThrowNotFound_whenIdDoesNotExist() {
            when(franchiseRepository.findById(99L)).thenReturn(Mono.empty());

            StepVerifier.create(franchiseService.getFranchiseById(99L))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }

    // ── getTopStockProductsByFranchise ────────────────────────────────────────

    @Nested
    @DisplayName("getTopStockProductsByFranchise")
    class GetTopStockProductsByFranchise {

        @Test
        @DisplayName("debe retornar el top stock por sucursal cuando la franquicia existe")
        void shouldReturnTopStock_whenFranchiseExists() {
            TopStockProduct tsp = new TopStockProduct(1L, "Sucursal Bogotá", 3L, "Papas Medianas", 300);

            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(buildFranchise()));
            when(franchiseRepository.findTopStockProductsByFranchiseId(FRANCHISE_ID))
                    .thenReturn(Flux.just(tsp));

            StepVerifier.create(franchiseService.getTopStockProductsByFranchise(FRANCHISE_ID))
                    .expectNext(tsp)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la franquicia no existe")
        void shouldThrowNotFound_whenFranchiseDoesNotExist() {
            when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(franchiseService.getTopStockProductsByFranchise(FRANCHISE_ID))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(franchiseRepository, never()).findTopStockProductsByFranchiseId(any());
        }
    }
}