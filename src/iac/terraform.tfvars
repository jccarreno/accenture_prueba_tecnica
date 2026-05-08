# ── General ───────────────────────────────────────────────────
project_name = "franchise-api"
environment  = "local"

# ── Docker ────────────────────────────────────────────────────
# Linux / Mac:
# docker_host = "unix:///var/run/docker.sock"

# Windows Docker Desktop (descomenta si usas Windows):
docker_host = "npipe:////./pipe/docker_engine"

# IMPORTANTE: cambia esta ruta a la ruta absoluta de tu proyecto.
# Debe apuntar a la carpeta donde está el pom.xml.
# Ejemplos:
#   Linux/Mac : "/home/usuario/proyectos/accenture_prueba_tecnica"
#   Windows   : "D:/accenture_prueba_tecnica"
build_context = "D:/accenture_prueba_tecnica"

api_version = "latest"

# ── Base de datos ──────────────────────────────────────────────
db_name      = "franchise_db"
db_user      = "root"
db_password  = "admin"        # En prod vendría de AWS Secrets Manager
db_port_host = 3306

# ── Redis ──────────────────────────────────────────────────────
redis_max_memory = "128mb"

# ── API ────────────────────────────────────────────────────────
api_port  = 8080
java_opts = "-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"