FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="franchise-api"
LABEL org.opencontainers.image.description="Franchise API — Prueba técnica Accenture Backend"
LABEL org.opencontainers.image.version="0.0.1-SNAPSHOT"
LABEL org.opencontainers.image.authors="accenture-prueba-tecnica"

WORKDIR /app

RUN apk add --no-cache wget

# Creamos un usuario no-root para ejecutar la aplicación.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos SOLO el JAR producido por el builder.
COPY --from=builder /workspace/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# ── Variables de entorno con valores por defecto ──────────────
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=docker \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=franchise_db \
    DB_USER=root \
    DB_PASSWORD=admin

# ── Healthcheck nativo de Docker ──────────────────────────────
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# ── Comando de arranque ───────────────────────────────────────
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]