package com.accenture.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para todos los tests de integración de infraestructura.
 *
 * <p>Levanta un único contenedor MySQL compartido (gracias al campo {@code static})
 * y registra dinámicamente las propiedades de conexión R2DBC y JDBC/Flyway en el
 * contexto de Spring. El contenedor se inicia una sola vez por suite y se destruye
 * al finalizar la JVM, minimizando el tiempo total de ejecución.</p>
 *
 * <p>Las subclases disponen de {@link #databaseClient} para ejecutar
 * sentencias de limpieza en {@code @BeforeEach} sin necesidad de inyectarlo
 * de nuevo en cada test.</p>
 *
 * <h3>Decisión de imagen</h3>
 * <p>{@code mysql:8.0} — misma versión mayor que producción, garantizando
 * paridad de comportamiento SQL (charset, collation, FK checks, etc.).</p>
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Contenedor MySQL compartido entre todos los tests de integración.
     * La anotación {@code static} hace que Testcontainers lo gestione como
     * recurso de clase y lo reutilice entre subclases en la misma JVM.
     */
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("franchise_test_db")
            .withUsername("test")
            .withPassword("test");

    /**
     * {@link DatabaseClient} reactivo inyectado para que las subclases
     * puedan ejecutar sentencias DDL/DML de limpieza entre tests.
     */
    @Autowired
    protected DatabaseClient databaseClient;

    /**
     * Registra las propiedades de conexión dinámicamente una vez que
     * Testcontainers asigna el puerto del contenedor.
     *
     * <p>Sobreescribe tanto la URL R2DBC (runtime reactivo) como la
     * URL JDBC del datasource (exclusivamente para Flyway al arranque).</p>
     *
     * @param registry registro de propiedades dinámicas de Spring Test
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306)
                + "/" + MYSQL.getDatabaseName() + "?allowPublicKeyRetrieval=true");
        registry.add("spring.r2dbc.username", MYSQL::getUsername);
        registry.add("spring.r2dbc.password", MYSQL::getPassword);

        registry.add("spring.datasource.url", () ->
                MYSQL.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    /**
     * Elimina todos los datos de las tres tablas en orden seguro respecto
     * a las FK ({@code product → branch → franchise}).
     *
     * <p>Se deshabilita temporalmente {@code FOREIGN_KEY_CHECKS} para poder
     * truncar las tablas sin importar el orden, y se vuelve a habilitar
     * al finalizar.</p>
     */
    protected void cleanAllTables() {
        databaseClient.sql("SET FOREIGN_KEY_CHECKS = 0").then().block();
        databaseClient.sql("DELETE FROM product").then().block();
        databaseClient.sql("DELETE FROM branch").then().block();
        databaseClient.sql("DELETE FROM franchise").then().block();
        databaseClient.sql("SET FOREIGN_KEY_CHECKS = 1").then().block();
    }
}