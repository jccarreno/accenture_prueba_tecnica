# =============================================================
#  main.tf — Infraestructura local con Terraform + Docker Provider
#
#  Uso:
#    terraform init
#    terraform apply
#    terraform destroy
# =============================================================

terraform {
  required_version = ">= 1.6"

  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
    # null provider: permite ejecutar comandos locales (docker build)
    # como workaround al bug del provider Docker en Windows.
    null = {
      source  = "hashicorp/null"
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

# Build de la imagen de la API via shell.
# El provider kreuzwerker/docker tiene un bug conocido en Windows
# que causa "unexpected EOF" al leer el Dockerfile en el build nativo.
# Usamos null_resource + local-exec como workaround: Terraform invoca
# directamente el CLI de Docker, que sí funciona correctamente en Windows.
resource "null_resource" "build_api_image" {
  # Re-construye la imagen si cambia el Dockerfile o el pom.xml.
  # En CI/CD esto se reemplazaría por el SHA del commit.
  triggers = {
    dockerfile_sha = filesha1("${var.build_context}/docker/Dockerfile")
    pom_sha        = filesha1("${var.build_context}/pom.xml")
  }

  provisioner "local-exec" {
    # Ejecuta 'docker build' desde la raíz del proyecto.
    # working_dir asegura que el contexto de build sea correcto
    # independientemente de desde dónde se ejecute Terraform.
    command     = "docker build -f docker/Dockerfile -t ${var.project_name}:${var.api_version} ."
    working_dir = var.build_context
  }
}

# Referencia a la imagen construida por null_resource.
# keep_locally = true evita que Terraform intente hacer pull desde
# un registry remoto — la imagen existe solo localmente.
resource "docker_image" "franchise_api" {
  name         = "${var.project_name}:${var.api_version}"
  keep_locally = true

  # Garantiza que el build termine antes de referenciar la imagen.
  depends_on = [null_resource.build_api_image]
}

# =============================================================
#  CONTENEDOR MySQL (simula Amazon RDS MySQL 8.0)
# =============================================================

resource "docker_container" "mysql" {
  name  = "${var.project_name}-mysql"
  image = docker_image.mysql.image_id

  restart = "unless-stopped"

  # Variables de entorno
  env = [
    "MYSQL_ROOT_PASSWORD=${var.db_password}",
    "MYSQL_DATABASE=${var.db_name}",
    "MYSQL_CHARACTER_SET_SERVER=utf8mb4",
    "MYSQL_COLLATION_SERVER=utf8mb4_unicode_ci",
  ]

  # Parámetros de rendimiento y compatibilidad de MySQL
  command = [
    "--character-set-server=utf8mb4",
    "--collation-server=utf8mb4_unicode_ci",
    "--default-authentication-plugin=mysql_native_password",
    "--innodb-buffer-pool-size=128M",
    "--max-connections=100",
  ]

  # Volumen persistente: los datos sobreviven reinicios del contenedor
  volumes {
    volume_name    = docker_volume.mysql_data.name
    container_path = "/var/lib/mysql"
  }

  # Puerto expuesto al host para herramientas locales (DBeaver, IntelliJ)
  ports {
    internal = 3306
    external = var.db_port_host
  }

  # Healthcheck: verifica que MySQL acepte conexiones antes de
  # que la API intente conectarse
  healthcheck {
    test         = ["CMD", "mysqladmin", "ping", "-h", "localhost",
                    "-u", "root", "-p${var.db_password}"]
    interval     = "10s"
    timeout      = "5s"
    retries      = 5
    start_period = "30s"
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
#  CONTENEDOR Redis
# =============================================================

resource "docker_container" "redis" {
  name  = "${var.project_name}-redis"
  image = docker_image.redis.image_id

  restart = "unless-stopped"

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

  # Terraform garantiza el orden: MySQL y Redis deben existir
  # antes de crear el contenedor de la API.
  depends_on = [
    docker_container.mysql,
    docker_container.redis,
  ]

  env = [
    "SPRING_PROFILES_ACTIVE=docker",
    # 'mysql' y 'redis' se resuelven por DNS interno de Docker
    # usando el nombre del contenedor en la misma red
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
