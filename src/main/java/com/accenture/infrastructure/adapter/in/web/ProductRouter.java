package com.accenture.infrastructure.adapter.in.web;

import com.accenture.shared.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Configuración de rutas funcionales de WebFlux para el recurso Producto.
 *
 * <p>Rutas expuestas:</p>
 * <ul>
 *   <li>{@code POST   /api/v1/products}                    — Agregar producto</li>
 *   <li>{@code DELETE /api/v1/products/{id}}               — Eliminar producto</li>
 *   <li>{@code PATCH  /api/v1/products/{id}/stock}         — Actualizar stock</li>
 *   <li>{@code PATCH  /api/v1/products/{id}/name}          — Actualizar nombre</li>
 *   <li>{@code GET    /api/v1/products/branch/{branchId}}  — Listar por sucursal</li>
 * </ul>
 */
@Configuration
public class ProductRouter {

    /**
     * Define el {@link RouterFunction} para el recurso Producto.
     *
     * @param handler handler con la lógica de cada operación
     * @return función de enrutamiento lista para ser registrada por Spring WebFlux
     */
    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .POST(ApiConstants.PRODUCTS_PATH, handler::add)
                .DELETE(ApiConstants.PRODUCTS_PATH + "/{id}", handler::delete)
                .PATCH(ApiConstants.PRODUCTS_PATH + "/{id}/stock", handler::updateStock)
                .PATCH(ApiConstants.PRODUCTS_PATH + "/{id}/name", handler::updateName)
                .GET(ApiConstants.PRODUCTS_PATH + "/branch/{branchId}", handler::getByBranch)
                .build();
    }
}