package com.accenture.domain.exception;

/**
 * Excepción de dominio lanzada cuando se intenta crear un recurso
 * que ya existe (violación de unicidad de negocio).
 * Mapeada a HTTP 409 en la capa de infraestructura.
 */
public class DuplicateResourceException extends RuntimeException {
 
    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;
 
    /**
     * @param resourceType Nombre del tipo de recurso
     * @param fieldName    Nombre del campo duplicado (ej: "name")
     * @param fieldValue   Valor duplicado
     */
    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
 
    public String getResourceType() {
        return resourceType;
    }
 
    public String getFieldName() {
        return fieldName;
    }
 
    public Object getFieldValue() {
        return fieldValue;
    }
}