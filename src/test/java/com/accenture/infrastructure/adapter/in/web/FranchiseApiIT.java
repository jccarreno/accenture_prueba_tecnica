package com.accenture.infrastructure.adapter.in.web;

import com.accenture.domain.model.Branch;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.Product;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.FranchiseRepository;
import com.accenture.domain.port.out.ProductRepository;
import com.accenture.infrastructure.AbstractIntegrationTest;
import com.accenture.shared.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Tests de integración end-to-end para los endpoints HTTP de la API.
 *
 * <p>Usa {@link WebTestClient} para lanzar peticiones HTTP reales contra un
 * servidor levantado en puerto aleatorio ({@code RANDOM_PORT}). La base de datos
 * MySQL se levanta con Testcontainers, con el esquema aplicado por Flyway.</p>
 *
 * <p>Cada test parte de un estado limpio gracias al {@code @BeforeEach}.</p>
 *
 * <p>Cubre los contratos HTTP: códigos de estado, estructura de {@link ApiResponse}
 * y comportamiento en casos de error (404, 409, 400).</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Franchise API — End-to-End Integration Tests")
class FranchiseApiIT extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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

    private Product saveProduct(Long branchId, String name, int stock) {
        return productRepository.save(Product.of(branchId, name, stock)).block();
    }

    // ── POST /api/v1/franchises ───────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/franchises")
    class CreateFranchise {

        @Test
        @DisplayName("201 — debe crear la franquicia con body válido")
        void shouldReturn201_whenValidBody() {
            webTestClient.post()
                    .uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"McDonald's IT\"}")
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.name").isEqualTo("McDonald's IT")
                    .jsonPath("$.data.id").isNotEmpty();
        }

        @Test
        @DisplayName("409 — debe rechazar nombre duplicado")
        void shouldReturn409_whenNameAlreadyExists() {
            saveFranchise("Franquicia Duplicada");

            webTestClient.post()
                    .uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"Franquicia Duplicada\"}")
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false);
        }

        @Test
        @DisplayName("400 — debe rechazar body con nombre en blanco")
        void shouldReturn400_whenNameIsBlank() {
            webTestClient.post()
                    .uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"\"}")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false);
        }
    }

    // ── GET /api/v1/franchises ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/franchises")
    class GetAllFranchises {

        @Test
        @DisplayName("200 — debe retornar lista de franquicias")
        void shouldReturn200_withFranchiseList() {
            saveFranchise("Franquicia 1");
            saveFranchise("Franquicia 2");

            webTestClient.get()
                    .uri("/api/v1/franchises")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.length()").isEqualTo(2);
        }
    }

    // ── GET /api/v1/franchises/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/franchises/{id}")
    class GetFranchiseById {

        @Test
        @DisplayName("200 — debe retornar la franquicia cuando existe")
        void shouldReturn200_whenFranchiseExists() {
            Franchise saved = saveFranchise("Franquicia por ID");

            webTestClient.get()
                    .uri("/api/v1/franchises/{id}", saved.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.id").isEqualTo(saved.id().intValue())
                    .jsonPath("$.data.name").isEqualTo("Franquicia por ID");
        }

        @Test
        @DisplayName("404 — debe retornar error cuando el ID no existe")
        void shouldReturn404_whenFranchiseNotFound() {
            webTestClient.get()
                    .uri("/api/v1/franchises/{id}", Long.MAX_VALUE)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false);
        }
    }

    // ── PATCH /api/v1/franchises/{id}/name ───────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/franchises/{id}/name")
    class UpdateFranchiseName {

        @Test
        @DisplayName("200 — debe actualizar el nombre correctamente")
        void shouldReturn200_whenUpdated() {
            Franchise saved = saveFranchise("Nombre Original");

            webTestClient.patch()
                    .uri("/api/v1/franchises/{id}/name", saved.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"Nombre Actualizado\"}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.name").isEqualTo("Nombre Actualizado");
        }

        @Test
        @DisplayName("404 — debe retornar error cuando la franquicia no existe")
        void shouldReturn404_whenFranchiseNotFound() {
            webTestClient.patch()
                    .uri("/api/v1/franchises/{id}/name", Long.MAX_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"Cualquier Nombre\"}")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    // ── GET /api/v1/franchises/{id}/top-stock ────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/franchises/{id}/top-stock")
    class GetTopStock {

        @Test
        @DisplayName("200 — debe retornar el producto de mayor stock por sucursal")
        void shouldReturn200_withTopStockPerBranch() {
            Franchise franchise = saveFranchise("Franquicia Top Stock IT");
            Branch    branch    = saveBranch(franchise.id(), "Sucursal Test");
            saveProduct(branch.id(), "Producto Stock Bajo",  10);
            saveProduct(branch.id(), "Producto Stock Alto", 999);

            webTestClient.get()
                    .uri("/api/v1/franchises/{id}/top-stock", franchise.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.length()").isEqualTo(1)
                    .jsonPath("$.data[0].stock").isEqualTo(999)
                    .jsonPath("$.data[0].productName").isEqualTo("Producto Stock Alto");
        }

        @Test
        @DisplayName("404 — debe retornar error cuando la franquicia no existe")
        void shouldReturn404_whenFranchiseNotFound() {
            webTestClient.get()
                    .uri("/api/v1/franchises/{id}/top-stock", Long.MAX_VALUE)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    // ── POST /api/v1/branches ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/branches")
    class AddBranch {

        @Test
        @DisplayName("201 — debe agregar sucursal a franquicia existente")
        void shouldReturn201_whenFranchiseExists() {
            Franchise franchise = saveFranchise("Franquicia para Branch");

            webTestClient.post()
                    .uri("/api/v1/branches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"franchiseId\": " + franchise.id() + ", \"name\": \"Sucursal IT\"}")
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.name").isEqualTo("Sucursal IT")
                    .jsonPath("$.data.franchiseId").isEqualTo(franchise.id().intValue());
        }

        @Test
        @DisplayName("404 — debe rechazar si la franquicia no existe")
        void shouldReturn404_whenFranchiseNotFound() {
            webTestClient.post()
                    .uri("/api/v1/branches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"franchiseId\": 99999, \"name\": \"Sucursal Huérfana\"}")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    // ── POST /api/v1/products ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/products")
    class AddProduct {

        @Test
        @DisplayName("201 — debe agregar producto a sucursal existente")
        void shouldReturn201_whenBranchExists() {
            Franchise franchise = saveFranchise("Franquicia para Producto");
            Branch    branch    = saveBranch(franchise.id(), "Sucursal para Producto");

            webTestClient.post()
                    .uri("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"branchId\": " + branch.id() + ", \"name\": \"Big Mac IT\", \"stock\": 100}")
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.name").isEqualTo("Big Mac IT")
                    .jsonPath("$.data.stock").isEqualTo(100);
        }

        @Test
        @DisplayName("400 — debe rechazar stock negativo")
        void shouldReturn400_whenStockIsNegative() {
            Franchise franchise = saveFranchise("Franquicia Stock Negativo");
            Branch    branch    = saveBranch(franchise.id(), "Sucursal Stock Negativo");

            webTestClient.post()
                    .uri("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"branchId\": " + branch.id() + ", \"name\": \"Producto\", \"stock\": -1}")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    // ── PATCH /api/v1/products/{id}/stock ────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/products/{id}/stock")
    class UpdateProductStock {

        @Test
        @DisplayName("200 — debe actualizar el stock correctamente")
        void shouldReturn200_whenUpdated() {
            Franchise franchise = saveFranchise("Franquicia Update Stock");
            Branch    branch    = saveBranch(franchise.id(), "Sucursal Update Stock");
            Product   product   = saveProduct(branch.id(), "Producto Update", 50);

            webTestClient.patch()
                    .uri("/api/v1/products/{id}/stock", product.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"stock\": 300}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true)
                    .jsonPath("$.data.stock").isEqualTo(300);
        }

        @Test
        @DisplayName("400 — debe rechazar tipo de dato incorrecto en stock")
        void shouldReturn400_whenStockIsNotANumber() {
            webTestClient.patch()
                    .uri("/api/v1/products/{id}/stock", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"stock\": \"no-es-numero\"}")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false);
        }
    }

    // ── DELETE /api/v1/products/{id} ──────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("200 — debe eliminar el producto cuando existe")
        void shouldReturn200_whenProductExists() {
            Franchise franchise = saveFranchise("Franquicia Delete");
            Branch    branch    = saveBranch(franchise.id(), "Sucursal Delete");
            Product   product   = saveProduct(branch.id(), "Producto a Eliminar", 10);

            webTestClient.delete()
                    .uri("/api/v1/products/{id}", product.id())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(true);
        }

        @Test
        @DisplayName("404 — debe retornar error cuando el producto no existe")
        void shouldReturn404_whenProductNotFound() {
            webTestClient.delete()
                    .uri("/api/v1/products/{id}", Long.MAX_VALUE)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}