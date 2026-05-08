package com.accenture.infrastructure.adapter.out.persistence;

import com.accenture.domain.model.Franchise;
import com.accenture.domain.model.TopStockProduct;
import com.accenture.domain.port.out.FranchiseRepository;
import com.accenture.infrastructure.adapter.out.persistence.mapper.FranchiseMapper;
import com.accenture.infrastructure.adapter.out.persistence.repository.FranchiseR2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida (puerto → infraestructura) para Franquicias.
 *
 * <p>Implementa el puerto de dominio {@link FranchiseRepository} delegando
 * en {@link FranchiseR2dbcRepository} para operaciones CRUD estándar y en
 * {@link DatabaseClient} para la query nativa de top-stock (que involucra
 * un JOIN entre tablas y una proyección personalizada {@link TopStockProduct}).</p>
 *
 * <h3>Query top-stock</h3>
 * <p>Usa una subconsulta correlacionada para obtener el producto con mayor
 * stock dentro de cada sucursal sin requerir window functions (compatible
 * con MySQL 5.7+):</p>
 * <pre>{@code
 * SELECT b.id, b.name, p.id, p.name, p.stock
 * FROM   branch b
 * JOIN   product p ON p.branch_id = b.id
 * WHERE  b.franchise_id = :franchiseId
 *   AND  p.stock = (SELECT MAX(p2.stock) FROM product p2 WHERE p2.branch_id = b.id)
 * }</pre>
 */
@Component
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseR2dbcRepository r2dbcRepository;
    private final DatabaseClient databaseClient;

    public FranchiseRepositoryAdapter(FranchiseR2dbcRepository r2dbcRepository,
                                      DatabaseClient databaseClient) {
        this.r2dbcRepository = r2dbcRepository;
        this.databaseClient = databaseClient;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Convierte el dominio a entidad, persiste con R2DBC y mapea el resultado de vuelta.</p>
     */
    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return r2dbcRepository.save(FranchiseMapper.toEntity(franchise))
                .map(FranchiseMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta el UPDATE parcial y luego busca la entidad actualizada para retornarla.</p>
     */
    @Override
    public Mono<Franchise> updateName(Long id, String newName) {
        return r2dbcRepository.updateNameById(id, newName)
                .then(r2dbcRepository.findById(id))
                .map(FranchiseMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Franchise> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(FranchiseMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Franchise> findAll() {
        return r2dbcRepository.findAll()
                .map(FranchiseMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> existsByName(String name) {
        return r2dbcRepository.existsByName(name);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta la query nativa via {@link DatabaseClient} y mapea cada fila
     * a una proyección {@link TopStockProduct}. Si una sucursal no tiene
     * ningún producto, no aparece en el resultado (INNER JOIN intencional).</p>
     */
    @Override
    public Flux<TopStockProduct> findTopStockProductsByFranchiseId(Long franchiseId) {
        String sql = """
                SELECT
                    b.id          AS branch_id,
                    b.name        AS branch_name,
                    p.id          AS product_id,
                    p.name        AS product_name,
                    p.stock       AS stock
                FROM branch b
                JOIN product p ON p.branch_id = b.id
                WHERE b.franchise_id = :franchiseId
                  AND p.stock = (
                      SELECT MAX(p2.stock)
                      FROM product p2
                      WHERE p2.branch_id = b.id
                  )
                ORDER BY b.id
                """;

        return databaseClient.sql(sql)
                .bind("franchiseId", franchiseId)
                .map((row, meta) -> new TopStockProduct(
                        row.get("branch_id",   Long.class),
                        row.get("branch_name", String.class),
                        row.get("product_id",  Long.class),
                        row.get("product_name",String.class),
                        row.get("stock",       Integer.class)
                ))
                .all();
    }
}