package com.accenture.domain.exception;

/**
 * Excepción de dominio lanzada cuando no se encuentra un recurso solicitado.
 * Mapeada a HTTP 404 en la capa de infraestructura.
 */
public class ResourceNotFoundException extends RuntimeException {
 
    private final String resourceType;
    private final Object resourceId;
 
    /**
     * @param resourceType Nombre del tipo de recurso (ej: "Franchise", "Branch", "Product")
     * @param resourceId   ID del recurso que no fue encontrado
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s with id '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
 
    public String getResourceType() {
        return resourceType;
    }
 
    public Object getResourceId() {
        return resourceId;
    }
}
