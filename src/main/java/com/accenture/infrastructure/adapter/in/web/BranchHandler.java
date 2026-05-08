package com.accenture.infrastructure.adapter.in.web;

import com.accenture.application.dto.request.CreateBranchRequest;
import com.accenture.application.dto.request.UpdateNameRequest;
import com.accenture.domain.model.Branch;
import com.accenture.domain.port.in.BranchUseCase;
import com.accenture.shared.ApiConstants;
import com.accenture.shared.ApiResponse;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional de WebFlux para operaciones sobre Sucursales.
 *
 * <p>Actúa como adaptador de entrada HTTP: extrae parámetros de la request,
 * delega en el use case y construye la {@link ServerResponse} apropiada.
 * No contiene lógica de negocio.</p>
 */
@Component
public class BranchHandler {

    private final BranchUseCase branchUseCase;

    public BranchHandler(BranchUseCase branchUseCase, Validator validator) {
        this.branchUseCase = branchUseCase;
    }

    /**
     * POST /api/v1/branches
     * Agrega una nueva sucursal a una franquicia existente.
     *
     * @param request petición HTTP con body {@link CreateBranchRequest}
     * @return 201 Created con la sucursal creada, o 400/404/409 según validación
     */
    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(CreateBranchRequest.class)
                .flatMap(HandlerUtils::validate)
                .map(dto -> Branch.of(dto.franchiseId(), dto.name()))
                .flatMap(branchUseCase::addBranch)
                .flatMap(created -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(ApiResponse.ok(ApiConstants.BRANCH_CREATED, created)));
    }

    /**
     * PATCH /api/v1/branches/{id}/name
     * Actualiza el nombre de una sucursal existente.
     *
     * @param request petición HTTP con path variable {@code id} y body {@link UpdateNameRequest}
     * @return 200 OK con la sucursal actualizada, o 404/409 según estado
     */
    public Mono<ServerResponse> updateName(ServerRequest request) {
        long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(HandlerUtils::validate)
                .flatMap(dto -> branchUseCase.updateBranchName(id, dto.name()))
                .flatMap(updated -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok(ApiConstants.BRANCH_UPDATED, updated)));
    }

    /**
     * GET /api/v1/branches/franchise/{franchiseId}
     * Retorna todas las sucursales de una franquicia.
     *
     * @param request petición HTTP con path variable {@code franchiseId}
     * @return 200 OK con lista de sucursales, o 404 si no existe la franquicia
     */
    public Mono<ServerResponse> getByFranchise(ServerRequest request) {
        long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        return branchUseCase.getBranchesByFranchise(franchiseId)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .bodyValue(ApiResponse.ok("Branches retrieved successfully", list)));
    }
}