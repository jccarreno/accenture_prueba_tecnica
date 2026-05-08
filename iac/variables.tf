# ── General ───────────────────────────────────────────────────

variable "project_name" {
  type        = string
  description = "Nombre base del proyecto. Se usa como prefijo en todos los recursos Docker (contenedores, red, volúmenes) para identificarlos fácilmente."
  default     = "franchise-api"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]*$", var.project_name))
    error_message = "El nombre del proyecto solo puede contener minúsculas, números y guiones, y debe empezar con una letra."
  }
}

variable "environment" {
  type        = string
  description = "Nombre del entorno de despliegue. Usado en labels de los recursos para identificar el contexto (local, dev, staging, prod)."
  default     = "local"

  validation {
    condition     = contains(["local", "dev", "staging", "prod"], var.environment)
    error_message = "El entorno debe ser uno de: local, dev, staging, prod."
  }
}

# ── Docker ────────────────────────────────────────────────────

variable "docker_host" {
  type        = string
  description = "Socket del daemon de Docker. Linux/Mac: unix:///var/run/docker.sock. Windows Docker Desktop: npipe:////./pipe/docker_engine"
  default     = "unix:///var/run/docker.sock"
}

variable "build_context" {
  type        = string
  description = "Ruta absoluta a la raíz del proyecto (donde está pom.xml). Terraform la usa como contexto de build del Dockerfile."
}

variable "api_version" {
  type        = string
  description = "Tag de la imagen Docker de la API. Usar 'latest' en desarrollo; en CI/CD usar el SHA del commit para trazabilidad."
  default     = "latest"
}

# ── Base de datos (simula Amazon RDS) ─────────────────────────

variable "db_name" {
  type        = string
  description = "Nombre de la base de datos MySQL a crear. Equivale al 'DB name' en la consola de Amazon RDS."
  default     = "franchise_db"
}

variable "db_user" {
  type        = string
  description = "Usuario administrador de MySQL. En Amazon RDS se llama 'Master username'."
  default     = "root"
}

variable "db_password" {
  type        = string
  description = "Contraseña del usuario administrador de MySQL. En producción vendría de AWS Secrets Manager, no de aquí."
  sensitive   = true 
  default     = "admin"
}

variable "db_port_host" {
  type        = number
  description = "Puerto del host donde se expone MySQL. Solo para herramientas locales (DBeaver, IntelliJ). En producción RDS no expone puertos públicamente."
  default     = 3306
}

# ── Redis (simula Amazon ElastiCache) ─────────────────────────

variable "redis_max_memory" {
  type        = string
  description = "Límite de memoria de Redis. 128mb simula un nodo cache.t3.micro de ElastiCache (~512MB real, reducido para entorno local)."
  default     = "128mb"
}

# ── API ────────────────────────────────────────────────────────

variable "api_port" {
  type        = number
  description = "Puerto del host donde se expone la API Spring Boot. Cambia si el 8080 está ocupado en tu máquina."
  default     = 8080
}

variable "java_opts" {
  type        = string
  description = "Opciones de la JVM para el contenedor de la API. UseContainerSupport hace que la JVM respete los límites de memoria del contenedor Docker."
  default     = "-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
}