# =============================================================
#  main.tf — Infraestructura local con Terraform + Docker Provider
#

terraform {
  required_version = ">= 1.6"

  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

# ── Provider ──────────────────────────────────────────────────
provider "docker" {
  host = var.docker_host
}

# =============================================================
#  RED (simula AWS VPC + Private Subnet)
# =============================================================

resource "docker_network" "franchise_network" {
  name   = "${var.project_name}-network"
  driver = "bridge"

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "environment"
    value = var.environment
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}

# =============================================================
#  VOLÚMENES (simulan Amazon EBS / RDS persistent storage)
# =============================================================

resource "docker_volume" "mysql_data" {
  name = "${var.project_name}-mysql-data"

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "component"
    value = "database"
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}

# Volumen de Redis — simula el snapshot de ElastiCache.
resource "docker_volume" "redis_data" {
  name = "${var.project_name}-redis-data"

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "component"
    value = "cache"
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}

# =============================================================
#  IMÁGENES
# =============================================================

# Imagen de MySQL 8.0 — descargada desde Docker Hub
resource "docker_image" "mysql" {
  name         = "mysql:8.0"
  keep_locally = true  
}

# Imagen de Redis 7 Alpine — descargada desde Docker Hub
resource "docker_image" "redis" {
  name         = "redis:7-alpine"
  keep_locally = true
}

# Imagen de la API — construida desde el Dockerfile local
resource "docker_image" "franchise_api" {
  name = "${var.project_name}:${var.api_version}"

  build {
    context    = var.build_context
    dockerfile = "docker/Dockerfile"

    build_args = {
      SKIP_TESTS = "true"
    }

    no_cache = false
  }

  force_remove = true

  triggers = {
    # Re-construye la imagen si cambia el Dockerfile o el código fuente.
    # En CI/CD esto sería reemplazado por el tag del commit (SHA).
    dir_sha1 = sha1(join("", [
      filesha1("${var.build_context}/docker/Dockerfile"),
      filesha1("${var.build_context}/pom.xml"),
    ]))
  }
}

# =============================================================
#  CONTENEDOR MySQL (simula Amazon RDS MySQL 8.0)
# =============================================================

resource "docker_container" "mysql" {
  name  = "${var.project_name}-mysql"
  image = docker_image.mysql.image_id

  restart = "unless-stopped"

  # Variables de entorno — en AWS vendrían de Secrets Manager
  env = [
    "MYSQL_ROOT_PASSWORD=${var.db_password}",
    "MYSQL_DATABASE=${var.db_name}",
    "MYSQL_CHARACTER_SET_SERVER=utf8mb4",
    "MYSQL_COLLATION_SERVER=utf8mb4_unicode_ci",
  ]

  # Comando de MySQL con configuraciones de rendimiento
  command = [
    "--character-set-server=utf8mb4",
    "--collation-server=utf8mb4_unicode_ci",
    "--default-authentication-plugin=mysql_native_password",
    "--innodb-buffer-pool-size=128M",
    "--max-connections=100",
  ]

  # Montar el volumen persistente
  volumes {
    volume_name    = docker_volume.mysql_data.name
    container_path = "/var/lib/mysql"
  }

  # Puerto expuesto al host
  ports {
    internal = 3306
    external = var.db_port_host
  }

  # Healthcheck: espera a que MySQL esté listo para aceptar conexiones
  healthcheck {
    test         = ["CMD", "mysqladmin", "ping", "-h", "localhost",
                    "-u", "root", "-p${var.db_password}"]
    interval     = "10s"
    timeout      = "5s"
    retries      = 5
    start_period = "30s"
  }

  # Conectar a la red interna
  networks_advanced {
    name = docker_network.franchise_network.name
  }

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "component"
    value = "database"
  }

  labels {
    label = "simulates"
    value = "Amazon RDS MySQL 8.0"
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}

# =============================================================
#  CONTENEDOR Redis (simula Amazon ElastiCache for Redis 7)
# =============================================================

resource "docker_container" "redis" {
  name  = "${var.project_name}-redis"
  image = docker_image.redis.image_id

  restart = "unless-stopped"

  # Configuración de Redis:
  #   --save 60 1         → persiste si hubo ≥1 cambio en 60s (RDB snapshot)
  #   --maxmemory         → simula el límite de un nodo t3.micro de ElastiCache
  #   --maxmemory-policy  → LRU: descarta keys menos usadas recientes
  command = [
    "redis-server",
    "--save", "60", "1",
    "--loglevel", "warning",
    "--maxmemory", "${var.redis_max_memory}",
    "--maxmemory-policy", "allkeys-lru",
  ]

  volumes {
    volume_name    = docker_volume.redis_data.name
    container_path = "/data"
  }

  # Redis NO expone su puerto al host por seguridad.
  # Solo los contenedores en la misma red pueden conectarse.
  # En producción ElastiCache tampoco es accesible públicamente.

  healthcheck {
    test         = ["CMD", "redis-cli", "ping"]
    interval     = "10s"
    timeout      = "3s"
    retries      = 3
    start_period = "10s"
  }

  networks_advanced {
    name = docker_network.franchise_network.name
  }

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "component"
    value = "cache"
  }

  labels {
    label = "simulates"
    value = "Amazon ElastiCache Redis 7"
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}

# =============================================================
#  CONTENEDOR API Spring Boot
# =============================================================

resource "docker_container" "franchise_api" {
  name  = "${var.project_name}-api"
  image = docker_image.franchise_api.image_id

  restart = "unless-stopped"

  # La API depende de que MySQL y Redis estén corriendo.
  # Terraform garantiza el orden de creación de recursos.
  depends_on = [
    docker_container.mysql,
    docker_container.redis,
  ]

  env = [
    "SPRING_PROFILES_ACTIVE=docker",
    "DB_HOST=${var.project_name}-mysql",
    "DB_PORT=3306",
    "DB_NAME=${var.db_name}",
    "DB_USER=${var.db_user}",
    "DB_PASSWORD=${var.db_password}",
    "REDIS_HOST=${var.project_name}-redis",
    "REDIS_PORT=6379",
    "SERVER_PORT=8080",
    "JAVA_OPTS=${var.java_opts}",
  ]

  ports {
    internal = 8080
    external = var.api_port
  }

  healthcheck {
    test = ["CMD-SHELL",
            "wget -qO- http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"' || exit 1"]
    interval     = "30s"
    timeout      = "10s"
    retries      = 3
    start_period = "90s"
  }

  networks_advanced {
    name = docker_network.franchise_network.name
  }

  labels {
    label = "project"
    value = var.project_name
  }

  labels {
    label = "component"
    value = "api"
  }

  labels {
    label = "managed-by"
    value = "terraform"
  }
}