package com.accenture;

import com.accenture.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test de arranque del contexto de Spring Boot.
 *
 * <p>Extiende {@link AbstractIntegrationTest} para levantar el contenedor
 * MySQL con Testcontainers y registrar las propiedades de conexión dinámicas.
 * Esto garantiza que el contexto completo (incluyendo Flyway, R2DBC y todos
 * los beans de infraestructura) arranque sin errores.</p>
 *
 * <p>Este test requiere Docker. Si Docker no está disponible, será ignorado
 * junto con los demás tests de integración ({@code *IT}).</p>
 */
@SpringBootTest
@DisplayName("Spring Boot Context — Smoke Test")
class PruebaTecnicaApplicationTests extends AbstractIntegrationTest {

    /**
     * Verifica que el contexto de Spring Boot arranca correctamente con todos
     * sus beans: repositorios R2DBC, handlers WebFlux, servicios de dominio,
     * configuración Flyway y manejador global de errores.
     */
    @Test
    @DisplayName("El contexto de Spring debe cargar sin errores")
    void contextLoads() {
        // Si llegamos aquí, todos los beans se inicializaron correctamente.
    }
}