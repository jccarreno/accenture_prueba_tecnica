# ============================================================
#  Dockerfile — Franchise API
#  Multi-stage build para imagen mínima de producción
# ============================================================

# ── Stage 1: Build ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiamos primero el POM y wrapper para aprovechar caché de capas Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargamos dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -q

# Copiamos el código fuente y compilamos
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadatos de la imagen
LABEL maintainer="Accenture Backend Team"
LABEL application="franchise-api"
LABEL version="1.0.0"

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos solo el JAR compilado desde la etapa de build
COPY --from=builder /app/target/*.jar franchise-api.jar

# Puerto de la aplicación
EXPOSE 8080

# Variables de entorno con valores por defecto (sobreescribibles en docker-compose)
ENV SPRING_PROFILES_ACTIVE=local \
    SERVER_PORT=8080             \
    DB_HOST=mysql                \
    DB_PORT=3306                 \
    DB_NAME=franchise_db         \
    DB_USER=franchise_user       \
    DB_PASSWORD=franchise_pass

# Opciones JVM optimizadas para contenedores
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "franchise-api.jar"]