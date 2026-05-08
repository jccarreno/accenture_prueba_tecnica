# accenture_prueba_tecnica

Prueba técnica de Api de control de franquicias para vacante de la empres Accenture

# Franchise API

API REST reactiva para la gestión de franquicias, sucursales y productos.
Prueba técnica Accenture — Backend Java.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Prerrequisitos](#prerrequisitos)
- [Despliegue con Docker Compose](#despliegue-con-docker-compose-recomendado)
- [Despliegue con Terraform (IaC)](#despliegue-con-terraform-iac)
- [Despliegue local sin Docker](#despliegue-local-sin-docker)
- [Referencia de la API](#referencia-de-la-api)
- [Ejecutar tests](#ejecutar-tests)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Variables de entorno](#variables-de-entorno)

---

## Descripción general

Franchise API permite administrar una red de franquicias compuesta por sucursales y productos. Expone endpoints REST sobre WebFlux funcional (sin `@RestController`) con programación completamente reactiva usando Project Reactor (`Mono` / `Flux`).

El esquema de base de datos es creado y versionado automáticamente por **Flyway** al arrancar la aplicación. No es necesario ejecutar ningún script SQL manualmente.

---

## Stack tecnológico

| Capa                  | Tecnología                              |
| --------------------- | ---------------------------------------- |
| Lenguaje              | Java 21                                  |
| Framework             | Spring Boot 4.0.6 + WebFlux              |
| Persistencia reactiva | Spring Data R2DBC + r2dbc-mysql          |
| Migraciones           | Flyway                                   |
| Base de datos         | MySQL 8.0                                |
| Cache / sesiones      | Redis 7 (preparado)                      |
| Validación           | Jakarta Validation (Hibernate Validator) |
| Build                 | Maven 3.9                                |
| Contenerización      | Docker + Docker Compose                  |
| IaC                   | Terraform >= 1.6 (provider Docker)       |
| Tests unitarios       | JUnit 5 + Mockito + StepVerifier         |
| Tests de integración | Testcontainers + WebTestClient           |

---

## Arquitectura

El proyecto sigue **Clean Architecture** con separación estricta en capas:

```
src/main/java/com/accenture/
├── domain/
│   ├── model/          ← Entidades puras (Java records): Franchise, Branch, Product
│   ├── port/
│   │   ├── in/         ← Interfaces de casos de uso (FranchiseUseCase, etc.)
│   │   └── out/        ← Interfaces de repositorio (FranchiseRepository, etc.)
│   └── exception/      ← Excepciones de dominio (ResourceNotFoundException, etc.)
│
├── application/
│   ├── service/        ← Implementación de los casos de uso
│   └── dto/            ← Request/Response DTOs (Java records)
│
├── infrastructure/
│   ├── adapter/
│   │   ├── in/web/     ← Router + Handler WebFlux funcional
│   │   └── out/persistence/ ← Adaptadores R2DBC, entidades, mappers
│   ├── config/         ← WebConfig (Validator bean)
│   └── exception/      ← GlobalErrorHandler
│
└── shared/             ← ApiResponse<T>, ApiConstants
```

La capa `domain` no depende de ninguna otra. La capa `application` solo conoce el dominio. La capa `infrastructure` implementa los puertos y contiene todos los detalles técnicos.

---

## Prerrequisitos

Elige **una** de las siguientes opciones según cómo quieras desplegar:

### Opción A — Docker Compose (recomendada)

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) >= 24.x
  Incluye Docker Engine y Docker Compose v2.

### Opción B — Terraform

- Docker Desktop >= 24.x
- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.6

### Opción C — Local sin Docker

- JDK 21 ([Eclipse Temurin](https://adoptium.net/))
- Maven 3.9+
- MySQL 8.0 corriendo en `localhost:3306`

---

## Despliegue con Docker Compose (recomendado)

Esta opción levanta tres contenedores automáticamente:

- `franchise-mysql` → MySQL 8.0 
- `franchise-redis` → Redis 7
- `franchise-api` → la aplicación Spring Boot

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd accenture_prueba_tecnica
```

### 2. Construir y levantar el stack

```bash
cd docker
docker compose up --build
```

La primera vez tarda 3-5 minutos porque Maven descarga las dependencias y construye la imagen. Las siguientes veces es más rápido gracias a la caché de capas Docker.

Verás en consola que el proceso termina cuando aparezca algo similar a:

```
franchise-api  | Started PruebaTecnicaApplication in 4.2 seconds
```

### 3. Verificar que todo está UP

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "r2dbc": { "status": "UP" }
  }
}
```

### 4. Usar la API

La API ya tiene datos de ejemplo cargados por Flyway. Puedes probarla de inmediato:

```bash
# Listar franquicias
curl http://localhost:8080/api/v1/franchises

# Ver el producto con mayor stock por sucursal de la franquicia 1
curl http://localhost:8080/api/v1/franchises/1/top-stock
```


---

## Despliegue con Terraform (IaC)

Terraform gestiona la misma infraestructura que Docker Compose pero como código versionado, demostrando prácticas profesionales de IaC que escalan a producción cloud.

### 1. Instalar Terraform

**Windows (Chocolatey):**

```powershell
choco install terraform
```

**Mac:**

```bash
brew tap hashicorp/tap && brew install hashicorp/tap/terraform
```

**Linux:**

```bash
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
```

### 2. Configurar variables locales

Edita `iac/terraform.tfvars` y ajusta `build_context` a la ruta absoluta de tu proyecto:

```hcl
# Windows:
build_context = "D:/accenture_prueba_tecnica"

# Linux / Mac:
build_context = "/home/tu-usuario/accenture_prueba_tecnica"

# Windows Docker Desktop (descomenta si aplica):
# docker_host = "npipe:////./pipe/docker_engine"
```

### 3. Aplicar la infraestructura

```bash
cd iac

# Descarga el provider de Docker para Terraform (solo la primera vez)
terraform init

# Muestra qué va a crear sin hacer nada (siempre recomendado antes de apply)
terraform plan

# Crea toda la infraestructura
terraform apply
```

Terraform pedirá confirmación. Escribe `yes` y presiona Enter.

Al terminar verás los outputs:

```
api_url                = "http://localhost:8080/api/v1"
api_health_url         = "http://localhost:8080/actuator/health"
mysql_connection_string = "jdbc:mysql://localhost:3306/franchise_db?..."
```

### 4. Destruir la infraestructura

```bash
terraform destroy
```

> Para más detalles sobre Terraform consulta `iac/README-IaC.md`.

---

## Despliegue local sin Docker

Usa esta opción si prefieres correr la aplicación directamente con Maven, con MySQL instalado en tu máquina.

### 1. Crear la base de datos en MySQL

```sql
CREATE DATABASE franchise_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

No es necesario crear tablas ni insertar datos — Flyway lo hace automáticamente al arrancar.

### 2. Verificar credenciales

El perfil `local` (activo por defecto) apunta a `localhost:3306` con usuario `root` y contraseña `admin`. Si tus credenciales son diferentes, edita `src/main/resources/application-local.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/franchise_db
    username: TU_USUARIO
    password: TU_CONTRASEÑA
  datasource:
    url: jdbc:mysql://localhost:3306/franchise_db?useSSL=false&allowPublicKeyRetrieval=true
    username: TU_USUARIO
    password: TU_CONTRASEÑA
```

### 3. Compilar y arrancar

```bash
# Compilar sin tests
mvn clean package -DskipTests

# Arrancar
mvn spring-boot:run

# O ejecutar el JAR directamente
java -jar target/prueba_tecnica-0.0.1-SNAPSHOT.jar
```

---

## Referencia de la API

Todas las respuestas siguen el mismo envoltorio:

```json
{
  "success": true,
  "message": "Descripción de la operación",
  "data": { ... },
  "timestamp": "2025-01-15T10:30:00"
}
```

En caso de error, `success` es `false` y `data` es `null`.

---

### Franquicias — `/api/v1/franchises`

#### `POST /api/v1/franchises`

Crea una nueva franquicia.

**Body:**

```json
{ "name": "McDonald's Colombia" }
```

**Respuesta `201 Created`:**

```json
{
  "success": true,
  "message": "Franchise created successfully",
  "data": { "id": 1, "name": "McDonald's Colombia", "branches": [] }
}
```

**Errores:** `400` nombre en blanco o mayor a 255 caracteres · `409` nombre duplicado

---

#### `GET /api/v1/franchises`

Retorna todas las franquicias.

**Respuesta `200 OK`:**

```json
{
  "success": true,
  "message": "Franchises retrieved successfully",
  "data": [
    { "id": 1, "name": "McDonald's Colombia", "branches": [] },
    { "id": 2, "name": "Burger King Colombia", "branches": [] }
  ]
}
```

---

#### `GET /api/v1/franchises/{id}`

Retorna una franquicia por ID.

**Errores:** `404` ID no encontrado

---

#### `PATCH /api/v1/franchises/{id}/name`

Actualiza el nombre de una franquicia.

**Body:**

```json
{ "name": "McDonald's Colombia Renovado" }
```

**Respuesta `200 OK`** — retorna la franquicia con el nombre actualizado.

**Errores:** `400` validación · `404` ID no encontrado · `409` nombre duplicado

---

#### `GET /api/v1/franchises/{id}/top-stock`

Retorna el producto con mayor stock por cada sucursal de la franquicia.

**Respuesta `200 OK`:**

```json
{
  "success": true,
  "message": "Top stock products retrieved successfully",
  "data": [
    {
      "branchId": 1,
      "branchName": "Sucursal Bogotá Centro",
      "productId": 3,
      "productName": "Papas Medianas",
      "stock": 300
    },
    {
      "branchId": 2,
      "branchName": "Sucursal Medellín El Poblado",
      "productId": 5,
      "productName": "McFlurry Oreo",
      "stock": 120
    }
  ]
}
```

**Errores:** `404` franquicia no encontrada

---

### Sucursales — `/api/v1/branches`

#### `POST /api/v1/branches`

Agrega una sucursal a una franquicia existente.

**Body:**

```json
{ "franchiseId": 1, "name": "Sucursal Cali Sur" }
```

**Respuesta `201 Created`**

**Errores:** `400` validación · `404` franquicia no encontrada · `409` nombre duplicado en la franquicia

---

#### `PATCH /api/v1/branches/{id}/name`

Actualiza el nombre de una sucursal.

**Body:**

```json
{ "name": "Sucursal Cali Centro" }
```

**Errores:** `400` · `404` · `409`

---

#### `GET /api/v1/branches/franchise/{franchiseId}`

Retorna todas las sucursales de una franquicia.

**Errores:** `404` franquicia no encontrada

---

### Productos — `/api/v1/products`

#### `POST /api/v1/products`

Agrega un producto a una sucursal existente.

**Body:**

```json
{ "branchId": 1, "name": "McRoyal", "stock": 150 }
```

**Respuesta `201 Created`**

**Errores:** `400` stock negativo o nombre en blanco · `404` sucursal no encontrada · `409` nombre duplicado en la sucursal

---

#### `DELETE /api/v1/products/{id}`

Elimina un producto.

**Respuesta `200 OK`**

**Errores:** `404` producto no encontrado

---

#### `PATCH /api/v1/products/{id}/stock`

Actualiza el stock de un producto.

**Body:**

```json
{ "stock": 500 }
```

**Errores:** `400` stock negativo · `404` producto no encontrado

---

#### `PATCH /api/v1/products/{id}/name`

Actualiza el nombre de un producto.

**Body:**

```json
{ "name": "McRoyal Deluxe" }
```

**Errores:** `400` · `404` · `409`

---

#### `GET /api/v1/products/branch/{branchId}`

Retorna todos los productos de una sucursal.

**Errores:** `404` sucursal no encontrada

---

### Códigos de error

| Código                       | Causa                                                                  |
| ----------------------------- | ---------------------------------------------------------------------- |
| `400 Bad Request`           | Validación fallida (campo en blanco, stock negativo, tipo incorrecto) |
| `404 Not Found`             | El recurso solicitado no existe                                        |
| `409 Conflict`              | Ya existe un recurso con ese nombre                                    |
| `500 Internal Server Error` | Error inesperado del servidor                                          |

---

## Ejecutar tests

### Unit tests (no requieren Docker)

Prueban la lógica de negocio de los servicios con Mockito. Rápidos, sin dependencias externas.

```bash
mvn test
```

Cubre `FranchiseService`, `BranchService` y `ProductService` — 32 casos de prueba en total verificando reglas de negocio, excepciones de dominio y propagación correcta de errores reactivos.

### Tests de integración (requieren Docker)

Prueban los adaptadores de persistencia y los endpoints HTTP completos contra MySQL real levantado con Testcontainers.

```bash
# Asegúrate de que Docker esté corriendo
mvn verify
```

Incluye:

- `FranchiseRepositoryAdapterIT` — operaciones de persistencia contra MySQL real
- `FranchiseApiIT` — suite end-to-end de todos los endpoints HTTP

### Omitir tests de integración

```bash
mvn verify -DskipITs
```

---

## Estructura del proyecto

```
accenture_prueba_tecnica/
│
├── src/
│   ├── main/
│   │   ├── java/com/accenture/          ← Código fuente (ver sección Arquitectura)
│   │   └── resources/
│   │       ├── application.yaml         ← Configuración base
│   │       ├── application-local.yml    ← Perfil desarrollo local
│   │       ├── application-docker.yml   ← Perfil Docker/contenedores
│   │       └── db/migration/
│   │           ├── V1__create_schema.sql  ← DDL: tablas franchise, branch, product
│   │           └── V2__seed_data.sql      ← Datos de ejemplo (2 franquicias, 5 sucursales, 15 productos)
│   └── test/
│       └── java/com/accenture/
│           ├── application/service/     ← Unit tests (Mockito + StepVerifier)
│           └── infrastructure/         ← Tests integración (Testcontainers + WebTestClient)
│
├── docker/
│   ├── Dockerfile                       ← Build multi-etapa (builder + runtime Alpine)
│   ├── docker-compose.yml               ← Stack completo: API + MySQL + Redis
│   └── .env                             ← Variables de entorno para docker-compose
│
├── iac/
│   ├── main.tf                          ← Recursos Terraform (red, volúmenes, contenedores)
│   ├── variables.tf                     ← Declaración de variables tipadas
│   ├── outputs.tf                       ← Valores exportados tras apply
│   ├── terraform.tfvars                 ← Valores concretos para entorno local
│   └── README-IaC.md                    ← Guía detallada de Terraform
│
└── pom.xml                              ← Dependencias Maven + Surefire/Failsafe
```

---

## Variables de entorno

Todas tienen valores por defecto para desarrollo local. Solo es necesario definirlas explícitamente en entornos distintos (staging, producción).

| Variable                   | Descripción               | Valor por defecto                          |
| -------------------------- | -------------------------- | ------------------------------------------ |
| `DB_HOST`                | Host de MySQL              | `localhost` (local) / `mysql` (Docker) |
| `DB_PORT`                | Puerto de MySQL            | `3306`                                   |
| `DB_NAME`                | Nombre de la base de datos | `franchise_db`                           |
| `DB_USER`                | Usuario de MySQL           | `root`                                   |
| `DB_PASSWORD`            | Contraseña de MySQL       | `admin`                                  |
| `REDIS_HOST`             | Host de Redis              | `localhost` (local) / `redis` (Docker) |
| `REDIS_PORT`             | Puerto de Redis            | `6379`                                   |
| `SERVER_PORT`            | Puerto HTTP de la API      | `8080`                                   |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo    | `local`                                  |
| `JAVA_OPTS`              | Opciones de la JVM         | `-Xms256m -Xmx512m`                      |

---

## Datos de ejemplo

Flyway carga automáticamente datos de ejemplo al arrancar. El estado inicial es:

| Franquicia                   | Sucursales                                           |
| ---------------------------- | ---------------------------------------------------- |
| McDonald's Colombia (id: 1)  | Bogotá Centro · Medellín El Poblado · Cali Norte |
| Burger King Colombia (id: 2) | Bogotá Chapinero · Barranquilla Centro             |

Cada sucursal tiene 3 productos con distintos niveles de stock para poder probar el endpoint `top-stock` de inmediato.
