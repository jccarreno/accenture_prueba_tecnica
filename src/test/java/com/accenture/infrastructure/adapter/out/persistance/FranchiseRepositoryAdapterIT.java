package com.accenture.infrastructure.adapter.out.persistance;

import com.accenture.domain.model.Branch;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.Product;
import com.accenture.domain.model.TopStockProduct;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.FranchiseRepository;
import com.accenture.domain.port.out.ProductRepository;
import com.accenture.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

/**
 * Tests de integración para {@link FranchiseRepositoryAdapter}.
 *
 * <p>Verifica el comportamiento real de las operaciones de persistencia
 * contra MySQL levantado con Testcontainers. Flyway ejecuta las migraciones
 * al iniciar el contexto, garantizando paridad de esquema con producción.</p>
 *
 * <p>Cada test parte de un estado limpio gracias al {@code @BeforeEach}
 * que elimina todos los registros de la base de datos de test.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("FranchiseRepositoryAdapter — Integration Tests")
class FranchiseRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private FranchiseRepository franchiseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Franchise saveFranchise(String name) {
        return franchiseRepository.save(Franchise.withName(name)).block();
    }

    private Branch saveBranch(Long franchiseId, String name) {
        return branchRepository.save(Branch.of(franchiseId, name)).block();
    }

    private void saveProduct(Long branchId, String name, int stock) {
        productRepository.save(Product.of(branchId, name, stock)).block();
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("debe persistir una franquicia nueva con ID autogenerado")
        void shouldSaveFranchiseWithGeneratedId() {
            StepVerifier.create(franchiseRepository.save(Franchise.withName("McDonald's Test")))
                    .expectNextMatches(f ->
                            f.id() != null &&
                            f.name().equals("McDonald's Test") &&
                            f.branches().isEmpty())
                    .verifyComplete();
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar la franquicia cuando el ID existe en BD")
        void shouldReturnFranchise_whenIdExists() {
            Franchise saved = saveFranchise("Burger King Test");

            StepVerifier.create(franchiseRepository.findById(saved.id()))
                    .expectNextMatches(f ->
                            f.id().equals(saved.id()) &&
                            f.name().equals("Burger King Test"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Mono vacío cuando el ID no existe en BD")
        void shouldReturnEmpty_whenIdNotFound() {
            StepVerifier.create(franchiseRepository.findById(Long.MAX_VALUE))
                    .verifyComplete();
        }
    }

    // ── existsByName ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("existsByName")
    class ExistsByName {

        @Test
        @DisplayName("debe retornar true cuando el nombre ya existe en BD")
        void shouldReturnTrue_whenNameExists() {
            saveFranchise("KFC Test");

            StepVerifier.create(franchiseRepository.existsByName("KFC Test"))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar false cuando el nombre no existe en BD")
        void shouldReturnFalse_whenNameDoesNotExist() {
            StepVerifier.create(franchiseRepository.existsByName("No Existe XYZ"))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    // ── updateName ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateName")
    class UpdateName {

        @Test
        @DisplayName("debe actualizar el nombre y persistir el cambio en BD")
        void shouldUpdateNameInDatabase() {
            Franchise saved = saveFranchise("Subway Original");

            StepVerifier.create(franchiseRepository.updateName(saved.id(), "Subway Renovado"))
                    .expectNextMatches(f ->
                            f.id().equals(saved.id()) &&
                            f.name().equals("Subway Renovado"))
                    .verifyComplete();

            // Verificar que el cambio persistió realmente en BD
            StepVerifier.create(franchiseRepository.findById(saved.id()))
                    .expectNextMatches(f -> f.name().equals("Subway Renovado"))
                    .verifyComplete();
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("debe retornar todas las franquicias existentes en BD")
        void shouldReturnAllPersistedFranchises() {
            saveFranchise("Franquicia A");
            saveFranchise("Franquicia B");
            saveFranchise("Franquicia C");

            StepVerifier.create(franchiseRepository.findAll().collectList())
                    .expectNextMatches(list -> list.size() == 3)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando no hay franquicias en BD")
        void shouldReturnEmpty_whenNone() {
            StepVerifier.create(franchiseRepository.findAll())
                    .verifyComplete();
        }
    }

    // ── findTopStockProductsByFranchiseId ─────────────────────────────────────

    @Nested
    @DisplayName("findTopStockProductsByFranchiseId")
    class FindTopStock {

        @Test
        @DisplayName("debe retornar el producto con mayor stock por cada sucursal")
        void shouldReturnHighestStockProductPerBranch() {
            Franchise franchise = saveFranchise("Franquicia Top Stock");
            Branch branch1 = saveBranch(franchise.id(), "Sucursal Norte");
            Branch branch2 = saveBranch(franchise.id(), "Sucursal Sur");

            // branch1: dos productos — debe ganar el de stock 200
            saveProduct(branch1.id(), "Producto Bajo",  50);
            saveProduct(branch1.id(), "Producto Alto", 200);

            // branch2: un único producto
            saveProduct(branch2.id(), "Único Producto", 75);

            StepVerifier.create(
                    franchiseRepository.findTopStockProductsByFranchiseId(franchise.id())
                            .collectList())
                    .expectNextMatches(list -> {
                        if (list.size() != 2) return false;

                        TopStockProduct top1 = list.stream()
                                .filter(t -> t.branchId().equals(branch1.id()))
                                .findFirst().orElse(null);
                        TopStockProduct top2 = list.stream()
                                .filter(t -> t.branchId().equals(branch2.id()))
                                .findFirst().orElse(null);

                        return top1 != null
                                && top1.stock() == 200
                                && top1.productName().equals("Producto Alto")
                                && top2 != null
                                && top2.stock() == 75
                                && top2.productName().equals("Único Producto");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando la sucursal no tiene productos")
        void shouldReturnEmpty_whenBranchHasNoProducts() {
            Franchise franchise = saveFranchise("Franquicia Sin Productos");
            saveBranch(franchise.id(), "Sucursal Vacía");

            StepVerifier.create(
                    franchiseRepository.findTopStockProductsByFranchiseId(franchise.id()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando la franquicia no existe")
        void shouldReturnEmpty_whenFranchiseDoesNotExist() {
            StepVerifier.create(
                    franchiseRepository.findTopStockProductsByFranchiseId(Long.MAX_VALUE))
                    .verifyComplete();
        }
    }
}