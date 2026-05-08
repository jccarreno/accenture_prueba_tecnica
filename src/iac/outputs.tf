# ── API ────────────────────────────────────────────────────────

output "api_url" {
  description = "URL base de la API. Pega esto en Postman o curl para probar los endpoints."
  value       = "http://localhost:${var.api_port}/api/v1"
}

output "api_health_url" {
  description = "Endpoint de Actuator para verificar que la aplicación está UP."
  value       = "http://localhost:${var.api_port}/actuator/health"
}

output "api_container_name" {
  description = "Nombre del contenedor Docker de la API. Úsalo con 'docker logs <nombre>'."
  value       = docker_container.franchise_api.name
}

# ── Base de datos ──────────────────────────────────────────────

output "mysql_container_name" {
  description = "Nombre del contenedor MySQL."
  value       = docker_container.mysql.name
}

output "mysql_connection_string" {
  description = "Cadena de conexión JDBC para clientes locales (DBeaver, IntelliJ). La contraseña se omite del output por seguridad."
  value       = "jdbc:mysql://localhost:${var.db_port_host}/${var.db_name}?useSSL=false&allowPublicKeyRetrieval=true"
  sensitive   = false
}

output "mysql_admin_user" {
  description = "Usuario administrador de MySQL."
  value       = var.db_user
}

# ── Redis ──────────────────────────────────────────────────────

output "redis_container_name" {
  description = "Nombre del contenedor Redis."
  value       = docker_container.redis.name
}

output "redis_internal_url" {
  description = "URL de Redis accesible SOLO desde dentro de la red Docker. La API usa esta URL."
  value       = "redis://${var.project_name}-redis:6379"
}

# ── Red ────────────────────────────────────────────────────────

output "docker_network_name" {
  description = "Nombre de la red Docker interna que conecta todos los servicios."
  value       = docker_network.franchise_network.name
}

# ── Resumen ────────────────────────────────────────────────────

output "stack_summary" {
  description = "Resumen del stack desplegado para referencia rápida."
  value = {
    project     = var.project_name
    environment = var.environment
    services = {
      api   = "http://localhost:${var.api_port}"
      mysql = "localhost:${var.db_port_host}/${var.db_name}"
      redis = "localhost:6379 (solo accesible desde la red interna Docker)"
    }
    simulates = {
      mysql = "Amazon RDS MySQL 8.0"
      redis = "Amazon ElastiCache for Redis 7"
      api   = "Amazon ECS / EC2 instance"
    }
  }
}