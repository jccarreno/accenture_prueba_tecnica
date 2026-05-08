package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Branch;
import com.accenture.domain.model.Product;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.ProductRepository;
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
 * Pruebas unitarias para {@link ProductService}.
 *
 * <p>Verifica las reglas de negocio: existencia de la sucursal padre,
 * unicidad de nombre dentro de la sucursal, y existencia del producto
 * antes de modificarlo, eliminarlo o actualizar su stock.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService — Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private ProductService productService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final Long   BRANCH_ID    = 10L;
    private static final Long   PRODUCT_ID   = 100L;
    private static final String PRODUCT_NAME = "Big Mac";
    private static final int    STOCK        = 150;

    private Branch buildBranch() {
        return new Branch(BRANCH_ID, 1L, "Sucursal Bogotá Centro", List.of());
    }

    private Product buildProduct() {
        return new Product(PRODUCT_ID, BRANCH_ID, PRODUCT_NAME, STOCK);
    }

    // ── addProduct ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addProduct")
    class AddProduct {

        @Test
        @DisplayName("debe agregar el producto cuando la sucursal existe y el nombre es único")
        void shouldAddProduct_whenBranchExistsAndNameIsUnique() {
            Product input = Product.of(BRANCH_ID, PRODUCT_NAME, STOCK);
            Product saved = buildProduct();

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(productRepository.existsByBranchIdAndName(BRANCH_ID, PRODUCT_NAME))
                    .thenReturn(Mono.just(false));
            when(productRepository.save(any())).thenReturn(Mono.just(saved));

            StepVerifier.create(productService.addProduct(input))
                    .expectNextMatches(p ->
                            p.id().equals(PRODUCT_ID) &&
                            p.branchId().equals(BRANCH_ID) &&
                            p.name().equals(PRODUCT_NAME) &&
                            p.stock().equals(STOCK))
                    .verifyComplete();

            verify(branchRepository).findById(BRANCH_ID);
            verify(productRepository).existsByBranchIdAndName(BRANCH_ID, PRODUCT_NAME);
            verify(productRepository).save(any());
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la sucursal no existe")
        void shouldThrowNotFound_whenBranchDoesNotExist() {
            Product input = Product.of(BRANCH_ID, PRODUCT_NAME, STOCK);

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.addProduct(input))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nombre ya existe en la sucursal")
        void shouldThrowDuplicate_whenNameAlreadyExistsInBranch() {
            Product input = Product.of(BRANCH_ID, PRODUCT_NAME, STOCK);

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(productRepository.existsByBranchIdAndName(BRANCH_ID, PRODUCT_NAME))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(productService.addProduct(input))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(productRepository, never()).save(any());
        }
    }

    // ── deleteProduct ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("debe eliminar el producto cuando existe")
        void shouldDeleteProduct_whenProductExists() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(buildProduct()));
            when(productRepository.deleteById(PRODUCT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.deleteProduct(PRODUCT_ID))
                    .verifyComplete();

            verify(productRepository).findById(PRODUCT_ID);
            verify(productRepository).deleteById(PRODUCT_ID);
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando el producto no existe")
        void shouldThrowNotFound_whenProductDoesNotExist() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.deleteProduct(PRODUCT_ID))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(productRepository, never()).deleteById(any());
        }
    }

    // ── updateStock ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStock")
    class UpdateStock {

        @Test
        @DisplayName("debe actualizar el stock cuando el producto existe")
        void shouldUpdateStock_whenProductExists() {
            int newStock     = 999;
            Product updated  = new Product(PRODUCT_ID, BRANCH_ID, PRODUCT_NAME, newStock);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(buildProduct()));
            when(productRepository.updateStock(PRODUCT_ID, newStock)).thenReturn(Mono.just(updated));

            StepVerifier.create(productService.updateStock(PRODUCT_ID, newStock))
                    .expectNextMatches(p -> p.stock().equals(newStock))
                    .verifyComplete();

            verify(productRepository).findById(PRODUCT_ID);
            verify(productRepository).updateStock(PRODUCT_ID, newStock);
        }

        @Test
        @DisplayName("debe aceptar stock = 0 (límite inferior válido)")
        void shouldAcceptZeroStock() {
            Product updated = new Product(PRODUCT_ID, BRANCH_ID, PRODUCT_NAME, 0);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(buildProduct()));
            when(productRepository.updateStock(PRODUCT_ID, 0)).thenReturn(Mono.just(updated));

            StepVerifier.create(productService.updateStock(PRODUCT_ID, 0))
                    .expectNextMatches(p -> p.stock().equals(0))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando el producto no existe")
        void shouldThrowNotFound_whenProductDoesNotExist() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.updateStock(PRODUCT_ID, 10))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(productRepository, never()).updateStock(any(), any());
        }
    }

    // ── updateProductName ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProductName")
    class UpdateProductName {

        @Test
        @DisplayName("debe actualizar el nombre cuando el producto existe y el nombre es único en la sucursal")
        void shouldUpdateName_whenProductExistsAndNameIsUnique() {
            String newName  = "McPollo Deluxe";
            Product updated = new Product(PRODUCT_ID, BRANCH_ID, newName, STOCK);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(buildProduct()));
            when(productRepository.existsByBranchIdAndName(BRANCH_ID, newName))
                    .thenReturn(Mono.just(false));
            when(productRepository.updateName(PRODUCT_ID, newName)).thenReturn(Mono.just(updated));

            StepVerifier.create(productService.updateProductName(PRODUCT_ID, newName))
                    .expectNextMatches(p -> p.name().equals(newName))
                    .verifyComplete();

            verify(productRepository).findById(PRODUCT_ID);
            verify(productRepository).existsByBranchIdAndName(BRANCH_ID, newName);
            verify(productRepository).updateName(PRODUCT_ID, newName);
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando el producto no existe")
        void shouldThrowNotFound_whenProductDoesNotExist() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.updateProductName(PRODUCT_ID, "Nuevo"))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(productRepository, never()).updateName(any(), any());
        }

        @Test
        @DisplayName("debe lanzar DuplicateResourceException cuando el nuevo nombre ya existe en la sucursal")
        void shouldThrowDuplicate_whenNewNameAlreadyTakenInBranch() {
            String newName = "McPollo";

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Mono.just(buildProduct()));
            when(productRepository.existsByBranchIdAndName(BRANCH_ID, newName))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(productService.updateProductName(PRODUCT_ID, newName))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(productRepository, never()).updateName(any(), any());
        }
    }

    // ── getProductsByBranch ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductsByBranch")
    class GetProductsByBranch {

        @Test
        @DisplayName("debe retornar los productos cuando la sucursal existe")
        void shouldReturnProducts_whenBranchExists() {
            Product p1 = buildProduct();
            Product p2 = new Product(101L, BRANCH_ID, "McPollo", 200);

            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(productRepository.findByBranchId(BRANCH_ID)).thenReturn(Flux.just(p1, p2));

            StepVerifier.create(productService.getProductsByBranch(BRANCH_ID))
                    .expectNext(p1)
                    .expectNext(p2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe retornar Flux vacío cuando la sucursal existe pero no tiene productos")
        void shouldReturnEmpty_whenBranchHasNoProducts() {
            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.just(buildBranch()));
            when(productRepository.findByBranchId(BRANCH_ID)).thenReturn(Flux.empty());

            StepVerifier.create(productService.getProductsByBranch(BRANCH_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("debe lanzar ResourceNotFoundException cuando la sucursal no existe")
        void shouldThrowNotFound_whenBranchDoesNotExist() {
            when(branchRepository.findById(BRANCH_ID)).thenReturn(Mono.empty());

            StepVerifier.create(productService.getProductsByBranch(BRANCH_ID))
                    .expectError(ResourceNotFoundException.class)
                    .verify();

            verify(productRepository, never()).findByBranchId(any());
        }
    }
}