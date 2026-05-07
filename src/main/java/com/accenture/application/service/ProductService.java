package com.accenture.application.service;

import com.accenture.domain.exception.DuplicateResourceException;
import com.accenture.domain.exception.ResourceNotFoundException;
import com.accenture.domain.model.Product;
import com.accenture.domain.port.in.ProductUseCase;
import com.accenture.domain.port.out.BranchRepository;
import com.accenture.domain.port.out.ProductRepository;
import com.accenture.shared.ApiConstants;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación de los casos de uso de Producto.
 *
 * <p>Orquesta validaciones de negocio: verifica que la sucursal padre
 * exista, que no haya nombres duplicados y que el producto exista
 * antes de modificarlo o eliminarlo.</p>
 */
@Service
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public ProductService(ProductRepository productRepository,
                          BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que la sucursal exista y que el nombre del producto
     * sea único dentro de la sucursal.</p>
     */
    @Override
    public Mono<Product> addProduct(Product product) {
        return branchRepository.findById(product.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_BRANCH, product.branchId())))
                .flatMap(branch ->
                        productRepository.existsByBranchIdAndName(product.branchId(), product.name()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException(
                                ApiConstants.RESOURCE_PRODUCT, "name", product.name()));
                    }
                    return productRepository.save(product);
                });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que el producto exista antes de eliminarlo.</p>
     */
    @Override
    public Mono<Void> deleteProduct(Long productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_PRODUCT, productId)))
                .flatMap(product -> productRepository.deleteById(productId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que el producto exista antes de actualizar el stock.</p>
     */
    @Override
    public Mono<Product> updateStock(Long productId, Integer newStock) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_PRODUCT, productId)))
                .flatMap(product -> productRepository.updateStock(productId, newStock));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que el producto exista y que el nuevo nombre no colisione
     * con otro producto de la misma sucursal.</p>
     */
    @Override
    public Mono<Product> updateProductName(Long productId, String newName) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_PRODUCT, productId)))
                .flatMap(existing ->
                        productRepository.existsByBranchIdAndName(existing.branchId(), newName)
                                .flatMap(nameExists -> {
                                    if (Boolean.TRUE.equals(nameExists)) {
                                        return Mono.error(new DuplicateResourceException(
                                                ApiConstants.RESOURCE_PRODUCT, "name", newName));
                                    }
                                    return productRepository.updateName(productId, newName);
                                }));
    }

    /** {@inheritDoc} */
    @Override
    public Flux<Product> getProductsByBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ApiConstants.RESOURCE_BRANCH, branchId)))
                .thenMany(productRepository.findByBranchId(branchId));
    }
}