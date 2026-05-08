package com.accenture;  

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

/**
 * Punto de entrada de la aplicación Franchise API.
 *
 * <p>Al ubicarse en el paquete raíz {@code com.accenture}, Spring Boot escanea
 * automáticamente todos los subpaquetes: domain, application, infrastructure y shared.</p>
 *
 * <p>{@code @EnableR2dbcAuditing} activa el soporte de auditoría reactiva para que
 * {@code @CreatedDate} y {@code @LastModifiedDate} en las entidades se rellenen
 * automáticamente sin configuración adicional.</p>
 */
@SpringBootApplication
@EnableR2dbcAuditing
public class PruebaTecnicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PruebaTecnicaApplication.class, args);
    }
}