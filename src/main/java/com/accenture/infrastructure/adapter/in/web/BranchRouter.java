package com.accenture.infrastructure.adapter.in.web;

import com.accenture.shared.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Configuración de rutas funcionales de WebFlux para el recurso Sucursal.
 *
 * <p>Rutas expuestas:</p>
 * <ul>
 *   <li>{@code POST   /api/v1/branches}                          — Agregar sucursal</li>
 *   <li>{@code PATCH  /api/v1/branches/{id}/name}                — Actualizar nombre</li>
 *   <li>{@code GET    /api/v1/branches/franchise/{franchiseId}}   — Listar por franquicia</li>
 * </ul>
 */
@Configuration
public class BranchRouter {

    /**
     * Define el {@link RouterFunction} para el recurso Sucursal.
     *
     * @param handler handler con la lógica de cada operación
     * @return función de enrutamiento lista para ser registrada por Spring WebFlux
     */
    @Bean
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return RouterFunctions.route()
                .POST(ApiConstants.BRANCHES_PATH, handler::add)
                .PATCH(ApiConstants.BRANCHES_PATH + "/{id}/name", handler::updateName)
                .GET(ApiConstants.BRANCHES_PATH + "/franchise/{franchiseId}", handler::getByFranchise)
                .build();
    }
}