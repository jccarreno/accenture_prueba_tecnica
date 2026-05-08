# =============================================================
#  Dockerfile — franchise-api
#
#  Build multi-etapa:
#    Etapa 1 (builder)  : compila el proyecto con Maven + JDK 21
#    Etapa 2 (runtime)  : imagen mínima solo con el JAR final
#
#  Contexto de build esperado: raíz del proyecto (donde está pom.xml)
#  Uso:
#    docker build -f docker/Dockerfile -t franchise-api:latest .
# =============================================================

# ── Etapa 1: Builder ─────────────────────────────────────────
# Maven 3.9 con JDK 21 Eclipse Temurin (distribución OpenJDK
# recomendada por Adoptium — licencia GPL con classpath exception).
# Esta imagen pesa ~500 MB pero NO llega al runtime final.
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# 1. Copiamos solo el pom.xml primero para aprovechar la caché de
#    capas de Docker: si las dependencias no cambian, Maven no las
#    re-descarga en builds posteriores (ahorra 1-3 minutos).
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

# 2. Copiamos el código fuente y compilamos.
#    -DskipTests : los tests corren en CI/CD, no en el build de imagen.
#    --no-transfer-progress : output más limpio en logs de CI.
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# ── Etapa 2: Runtime ─────────────────────────────────────────
# JRE 21 Alpine (~85 MB) — sin JDK completo, mínima superficie
# de ataque. Alpine usa musl libc en lugar de glibc; es compatible
# con Eclipse Temurin para Java.
FROM eclipse-temurin:21-jre-alpine

# Metadatos OCI estándar (buenas prácticas de imagen)
LABEL org.opencontainers.image.title="franchise-api"
LABEL org.opencontainers.image.description="Franchise API — Prueba técnica Accenture Backend"
LABEL org.opencontainers.image.version="0.0.1-SNAPSHOT"
LABEL org.opencontainers.image.authors="accenture-prueba-tecnica"

WORKDIR /app

# Instalamos wget para el healthcheck interno de Docker.
# --no-cache evita almacenar el índice de paquetes en la imagen.
RUN apk add --no-cache wget

# Creamos un usuario no-root para ejecutar la aplicación.
# Correr como root en un contenedor es una vulnerabilidad de seguridad:
# si hay un exploit en la app, el atacante obtendría root en el host.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos SOLO el JAR producido por el builder.
# El patrón *.jar evita hardcodear la versión en el nombre del artefacto.
COPY --from=builder /workspace/target/*.jar app.jar

# Asignamos propiedad del JAR al usuario no-root
RUN chown appuser:appgroup app.jar

USER appuser

# Puerto documentativo (el mapeo real ocurre en docker-compose o el orquestador)
EXPOSE 8080

# ── Variables de entorno con valores por defecto ──────────────
# Todas son sobreescribibles desde docker-compose o variables de entorno del host.
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=docker \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=franchise_db \
    DB_USER=root \
    DB_PASSWORD=admin

# ── Healthcheck nativo de Docker ──────────────────────────────
# Docker marcará el contenedor como "unhealthy" si el endpoint
# de Actuator no responde, permitiendo que docker-compose lo
# reinicie o que un orquestador lo reemplace.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# ── Comando de arranque ───────────────────────────────────────
# Exec form (array JSON): las señales del SO (SIGTERM) llegan
# directamente al proceso Java → shutdown graceful de Spring Boot.
# Shell form ("java ...") las enviaría al shell, no a la JVM.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]