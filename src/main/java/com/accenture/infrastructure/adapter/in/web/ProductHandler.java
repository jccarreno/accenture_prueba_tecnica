package com.accenture.infrastructure.adapter.in.web;

import com.accenture.application.dto.request.CreateProductRequest;
import com.accenture.application.dto.request.UpdateNameRequest;
import com.accenture.application.dto.request.UpdateStockRequest;
import com.accenture.domain.model.Product;
import com.accenture.domain.port.in.ProductUseCase;
import com.accenture.shared.ApiConstants;
import com.accenture.shared.ApiResponse;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional de WebFlux para operaciones sobre Productos.
 *
 * <p>Actúa como adaptador de entrada HTTP: extrae parámetros de la request,
 * delega en el use case y construye la {@link ServerResponse} apropiada.
 * No contiene lógica de negocio.</p>
 */
@Component
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public ProductHandler(ProductUseCase productUseCase, Validator validator) {
        this.productUseCase = productUseCase;
    }

    /**
     * POST /api/v1/products
     * Agrega un nuevo producto a una sucursal existente.
     *
     * @param request petición HTTP con body {@link CreateProductRequest}
     * @return 201 Created con el producto creado, o 400/404/409 según validación
     */
    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(CreateProductRequest.class)
                .flatMap(HandlerUtils::validate)
                .map(dto -> Product.of(dto.branchId(), dto.name(), dto.stock()))
                .flatMap(productUseCase::addProduct)
                .flatMap(created -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(ApiResponse.ok(ApiConstants.PRODUCT_CREATED, created)));
    }

    /**
     * DELETE /api/v1/products/{id}
     * Elimina un producto de una sucursal.
     *
     * @param request petición HTTP con path variable {@code id}
     * @return 200 OK con mensaje de confirmación, o 404 si no existe
     */
    public Mono<ServerResponse> delete(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return productUseCase.deleteProduct(id)
                .then(ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.PRODUCT_DELETED)));
    }

    /**
     * PATCH /api/v1/products/{id}/stock
     * Actualiza el stock de un producto existente.
     *
     * @param request petición HTTP con path variable {@code id} y body {@link UpdateStockRequest}
     * @return 200 OK con el producto actualizado, o 400/404 según estado
     */
    public Mono<ServerResponse> updateStock(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(HandlerUtils::validate)
                .flatMap(dto -> productUseCase.updateStock(id, dto.stock()))
                .flatMap(updated -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.PRODUCT_STOCK_UPDATED, updated)));
    }

    /**
     * PATCH /api/v1/products/{id}/name
     * Actualiza el nombre de un producto existente.
     *
     * @param request petición HTTP con path variable {@code id} y body {@link UpdateNameRequest}
     * @return 200 OK con el producto actualizado, o 400/404/409 según estado
     */
    public Mono<ServerResponse> updateName(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(HandlerUtils::validate)
                .flatMap(dto -> productUseCase.updateProductName(id, dto.name()))
                .flatMap(updated -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.PRODUCT_NAME_UPDATED, updated)));
    }

    /**
     * GET /api/v1/products/branch/{branchId}
     * Retorna todos los productos de una sucursal.
     *
     * @param request petición HTTP con path variable {@code branchId}
     * @return 200 OK con lista de productos, o 404 si no existe la sucursal
     */
    public Mono<ServerResponse> getByBranch(ServerRequest request) {
        long branchId = Long.parseLong(request.pathVariable("branchId"));
        return productUseCase.getProductsByBranch(branchId)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok("Products retrieved successfully", list)));
    }
}