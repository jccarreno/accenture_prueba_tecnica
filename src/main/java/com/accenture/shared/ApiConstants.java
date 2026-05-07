package com.accenture.shared;

/**
 * Constantes transversales utilizadas en toda la aplicación.
 * Centraliza paths de API, mensajes de respuesta y otros literales reutilizables.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Clase de utilidad — no instanciar
    }

    // ── Base paths ────────────────────────────────────────────────────────────

    /** Prefijo base de la API */
    public static final String API_BASE = "/api/v1";

    /** Path base para franquicias */
    public static final String FRANCHISES_PATH = API_BASE + "/franchises";

    /** Path base para sucursales */
    public static final String BRANCHES_PATH = API_BASE + "/branches";

    /** Path base para productos */
    public static final String PRODUCTS_PATH = API_BASE + "/products";

    // ── Mensajes de éxito ────────────────────────────────────────────────────

    public static final String FRANCHISE_CREATED   = "Franchise created successfully";
    public static final String FRANCHISE_UPDATED   = "Franchise updated successfully";
    public static final String BRANCH_CREATED      = "Branch added successfully";
    public static final String BRANCH_UPDATED      = "Branch updated successfully";
    public static final String PRODUCT_CREATED     = "Product added successfully";
    public static final String PRODUCT_DELETED     = "Product deleted successfully";
    public static final String PRODUCT_STOCK_UPDATED = "Product stock updated successfully";
    public static final String PRODUCT_NAME_UPDATED  = "Product name updated successfully";
    public static final String TOP_STOCK_RETRIEVED = "Top stock products retrieved successfully";

    // ── Nombres de recursos (para excepciones) ───────────────────────────────

    public static final String RESOURCE_FRANCHISE = "Franchise";
    public static final String RESOURCE_BRANCH    = "Branch";
    public static final String RESOURCE_PRODUCT   = "Product";
}
