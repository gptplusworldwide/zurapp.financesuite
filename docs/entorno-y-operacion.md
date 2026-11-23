# Entorno local, configuración y operaciones

## Requisitos verificados en el proyecto

- Windows + Eclipse y Git Bash fueron utilizados en el desarrollo reportado; son reemplazables por otras plataformas con Java 21.
- Java 21 y Maven Wrapper (`mvnw`, `mvnw.cmd`), Docker Desktop con Compose, conexión al servicio Supabase Auth.
- PostgreSQL `postgres:17` en contenedor `finance-suite-postgres`, puerto host **127.0.0.1:5433** a puerto contenedor 5432, volumen persistente `postgres_data`.
- Pruebas Testcontainers inician PostgreSQL `postgres:17-alpine` temporal.

## Variables reales

| Variable | La utiliza | Función |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | `compose.yaml` | Password al inicializar PostgreSQL local |
| `DB_PASSWORD` | `application.properties` | Password JDBC Spring |
| `SUPABASE_URL` | `application.properties` | URL base de Supabase (`https://...supabase.co`) |

El código fija URL `jdbc:postgresql://localhost:5433/finance_suite` y usuario `finance_user` en `application.properties`; no están parametrizados actualmente. El `compose.yaml` define base `finance_suite` y usuario `finance_user`.

**La contraseña de Compose y JDBC debe coincidir.** Atención: cambiar `POSTGRES_PASSWORD` después de inicializado el volumen no cambia automáticamente la contraseña existente de PostgreSQL.

## Arranque recomendado (Git Bash)

```bash
cd /d/DataDeveloper/zsurapp/zsurapp
cp .env.example .env
# Ajustar .env -> POSTGRES_PASSWORD=...
docker compose --env-file .env up -d
docker compose --env-file .env ps

# Solo en esta sesión, nunca pegar un token real en el código:
export DB_PASSWORD='misma_clave_que_en_.env'
export SUPABASE_URL='https://TU-PROYECTO.supabase.co'
./mvnw spring-boot:run
```

O ejecutar `ZsurappApplication` desde Eclipse, configurando `DB_PASSWORD` y `SUPABASE_URL` en la configuración de ejecución. Las variables de Git Bash no siempre se heredan por Eclipse iniciado desde Windows.

## Confirmaciones de arranque

```text
Found 5 JPA repository interfaces
Successfully validated 6 migrations
Current version of schema "public": 6
Started ZsurappApplication
```

Las cuatro cadenas describen el **estado esperado de FS-008** en `feat/transfers`. En `main` pueden ser distintas mientras no se confirme la sincronización.

## Operaciones útiles

```bash
./mvnw clean compile
./mvnw clean test
./mvnw test -Dtest=TransferServiceTest
./mvnw test -Dtest=TransferRepositoryTest

docker compose --env-file .env logs postgres
docker compose --env-file .env stop
docker compose --env-file .env up -d
```

No usar `docker compose down -v` sobre un entorno con datos a conservar: elimina el volumen persistente.

Para consultar el historial Flyway (sin publicar contraseñas en logs):

```bash
docker compose --env-file .env exec postgres \
  psql -U finance_user -d finance_suite \
  -c 'SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;'
```

## Diagnóstico rápido

| Síntoma | Revisar |
| --- | --- |
| `Connection refused` a 5433 | Docker Desktop, Compose, puerto local, healthcheck |
| `password authentication failed` | `DB_PASSWORD` vs contraseña efectiva en volumen PostgreSQL |
| JWT 401 | Token expirado, emisor, audiencia, ES256, JWKS, `SUPABASE_URL` |
| Migration mismatch | Archivos V1–V6, historial Flyway y rama correcta |
| `Found 4 repositories` cuando espera 5 | Rama y compilación de `transfer.repository.TransferRepository` |
| Testcontainers falla | Docker en ejecución y acceso al daemon |
| Balance inesperado | Consultar transacciones y transferencias; evitar POST duplicado |

## Backup y producción

Todavía no hay procedimiento de backup/restauración o despliegue productivo verificado. Antes de crear esos sistemas documentar: políticas de retención, credenciales por entorno, TLS, copias y prueba restaurable, alertas y migraciones forward-only.
