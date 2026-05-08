package com.accenture.infrastructure.adapter.in.web;

import com.accenture.application.dto.request.CreateFranchiseRequest;
import com.accenture.application.dto.request.UpdateNameRequest;
import com.accenture.domain.model.Franchise;
import com.accenture.domain.port.in.FranchiseUseCase;
import com.accenture.shared.ApiConstants;
import com.accenture.shared.ApiResponse;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional de WebFlux para operaciones sobre Franquicias.
 *
 * <p>Actúa como adaptador de entrada HTTP: extrae parámetros de la request,
 * delega en el use case y construye la {@link ServerResponse} apropiada.
 * No contiene lógica de negocio.</p>
 */
@Component
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;

    public FranchiseHandler(FranchiseUseCase franchiseUseCase, Validator validator) {
        this.franchiseUseCase = franchiseUseCase;
    }

    /**
     * POST /api/v1/franchises
     * Crea una nueva franquicia.
     *
     * @param request petición HTTP con body {@link CreateFranchiseRequest}
     * @return 201 Created con la franquicia creada, o 400/409 según validación
     */
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .flatMap(HandlerUtils::validate)
                .map(dto -> Franchise.withName(dto.name()))
                .flatMap(franchiseUseCase::createFranchise)
                .flatMap(created -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(ApiResponse.ok(ApiConstants.FRANCHISE_CREATED, created)));
    }

    /**
     * PATCH /api/v1/franchises/{id}/name
     * Actualiza el nombre de una franquicia existente.
     *
     * @param request petición HTTP con path variable {@code id} y body {@link UpdateNameRequest}
     * @return 200 OK con la franquicia actualizada, o 404/409 según estado
     */
    public Mono<ServerResponse> updateName(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(HandlerUtils::validate)
                .flatMap(dto -> franchiseUseCase.updateFranchiseName(id, dto.name()))
                .flatMap(updated -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.FRANCHISE_UPDATED, updated)));
    }

    /**
     * GET /api/v1/franchises
     * Retorna todas las franquicias registradas.
     *
     * @param request petición HTTP
     * @return 200 OK con lista de franquicias
     */
    public Mono<ServerResponse> getAll(ServerRequest request) {
        return franchiseUseCase.getAllFranchises()
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok("Franchises retrieved successfully", list)));
    }

    /**
     * GET /api/v1/franchises/{id}
     * Retorna una franquicia por su ID.
     *
     * @param request petición HTTP con path variable {@code id}
     * @return 200 OK con la franquicia, o 404 si no existe
     */
    public Mono<ServerResponse> getById(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return franchiseUseCase.getFranchiseById(id)
                .flatMap(franchise -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok("Franchise retrieved successfully", franchise)));
    }

    /**
     * GET /api/v1/franchises/{id}/top-stock
     * Retorna el producto con mayor stock por sucursal para una franquicia.
     *
     * @param request petición HTTP con path variable {@code id}
     * @return 200 OK con la lista de top-stock por sucursal, o 404 si no existe la franquicia
     */
    public Mono<ServerResponse> getTopStock(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return franchiseUseCase.getTopStockProductsByFranchise(id)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.TOP_STOCK_RETRIEVED, list)));
    }
}