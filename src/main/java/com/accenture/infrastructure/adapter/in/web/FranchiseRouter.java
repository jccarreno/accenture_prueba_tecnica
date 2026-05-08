package com.accenture.infrastructure.adapter.in.web;

import com.accenture.shared.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Configuración de rutas funcionales de WebFlux para el recurso Franquicia.
 *
 * <p>Declara explícitamente cada ruta y la asocia con su método handler.
 * Al usar el estilo funcional (RouterFunction + Handler) en lugar de {@code @RestController},
 * se obtiene mayor control sobre el flujo reactivo y se evita el overhead de la reflexión
 * de Spring MVC.</p>
 *
 * <p>Rutas expuestas:</p>
 * <ul>
 *   <li>{@code POST   /api/v1/franchises}            — Crear franquicia</li>
 *   <li>{@code GET    /api/v1/franchises}             — Listar franquicias</li>
 *   <li>{@code GET    /api/v1/franchises/{id}}        — Obtener por ID</li>
 *   <li>{@code PATCH  /api/v1/franchises/{id}/name}   — Actualizar nombre</li>
 *   <li>{@code GET    /api/v1/franchises/{id}/top-stock} — Top stock por sucursal</li>
 * </ul>
 */
@Configuration
public class FranchiseRouter {

    /**
     * Define el {@link RouterFunction} para el recurso Franquicia.
     *
     * @param handler handler con la lógica de cada operación
     * @return función de enrutamiento lista para ser registrada por Spring WebFlux
     */
    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST(ApiConstants.FRANCHISES_PATH, handler::create)
                .GET(ApiConstants.FRANCHISES_PATH, handler::getAll)
                .GET(ApiConstants.FRANCHISES_PATH + "/{id}", handler::getById)
                .PATCH(ApiConstants.FRANCHISES_PATH + "/{id}/name", handler::updateName)
                .GET(ApiConstants.FRANCHISES_PATH + "/{id}/top-stock", handler::getTopStock)
                .build();
    }
}