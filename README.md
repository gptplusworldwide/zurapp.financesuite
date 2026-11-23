# Finance Suite — Estado actual y guía de desarrollo

- **Corte documental:** 23 de noviembre de 2026
- **Producto:** Finance Suite / backend `zsurapp`
- **Repositorio:** https://github.com/gptplusworldwide/zurapp.financesuite
- **Paquete base:** `com.financesuite.zsurapp`
- **Versión Maven observada:** `0.0.1-SNAPSHOT`
**Estado:** backend financiero funcional; no confundirlo con el MVP PWA completo.

> **Procedencia y niveles de certeza.** Se revisó el código publicado de la rama `feat/transfers` (commit `955ed198bd626881e7e973b8ebf1b2fa29db83c7`), archivos de `main` y la documentación inicial entregada por el usuario. La ejecución de **112 pruebas sin fallos** y la integración en `main` fueron **reportadas por el desarrollador**, no ejecutadas en este entorno. El 23/11/2026 la consulta remota a `main` (`7783e7ce801fbccddd82d1db4608c0373ac8b3b9`) aún mostraba V1–V5; `feat/transfers` mostraba V1–V6. Ver [verificación de ramas](docs/continuidad.md) antes del próximo trabajo.

## Qué es

Finance Suite busca ser una suite web/PWA, con una identidad de usuario y varios módulos. **El primer módulo, Finanzas**, permite administrar espacios financieros independientes `PERSONAL` y `BUSINESS`. En la implementación actual cada espacio tiene un `owner_id` único y dispone de cuentas, categorías y operaciones propias. La suite, frontend PWA, colaboración y dashboard siguen siendo objetivos futuros.

## Lo que existe hoy

| Componente | Estado | Observaciones |
| --- | --- | --- |
| API REST Spring Boot / Java 21 | Implementado | Puerto local `8080`; `/api/v1` |
| JWT Supabase Auth | Implementado | Spring Security OAuth2 Resource Server |
| Workspaces | Implementado | Crear, listar, obtener, renombrar; propietario único |
| Cuentas | Implementado | Crear, listar, obtener, renombrar; saldo inicial por cuenta |
| Categorías | Implementado | `INCOME` y `EXPENSE` |
| Ingresos y gastos | Implementado | Crear, listar, obtener; sin edición/borrado |
| Transferencias | Implementado en `feat/transfers` | Crear, listar, obtener; misma moneda y espacio |
| Saldos derivados | Implementado en `feat/transfers` | Inicial + ingresos − gastos + recibidas − enviadas |
| Flyway | V1 a V6 | V6 en rama `feat/transfers` |
| Pruebas automatizadas | 112 reportadas como exitosas | JUnit, MockMvc/Mockito, PostgreSQL Testcontainers |
| Dashboard, frontend React/PWA, despliegue | No verificado como implementado | Planificados; no aparecen en el árbol de código inspeccionado |

## Stack confirmado en el código

- Java **21**, Spring Boot **4.2.0**, Maven Wrapper.
- Spring MVC, Bean Validation, Spring Security y OAuth2 Resource Server con JWT.
- Spring Data JPA, Hibernate, Flyway, PostgreSQL **17** local vía Docker Compose.
- SQL monetario `NUMERIC(19,4)`; Java `BigDecimal`.
- JUnit 5, Mockito, pruebas MVC y Testcontainers con PostgreSQL `17-alpine`.
- Supabase **Auth** para identidad; la base de datos operativa comprobada es **PostgreSQL local**. Alojamiento PostgreSQL en Supabase para producción sigue siendo una opción de arquitectura, no una implementación verificada.

## Ejecutar localmente

Requisitos: Java 21, Docker Desktop, Git Bash o terminal con Maven Wrapper, acceso a Supabase Auth configurado.

```bash
# Desde la raíz del repositorio (la app Java está en la raíz)
cp .env.example .env
# Editar .env con POSTGRES_PASSWORD real y exclusivo del entorno local

docker compose --env-file .env up -d

# Establecer en la sesión variables NO versionadas:
export DB_PASSWORD='tu_clave_local'
export SUPABASE_URL='https://TU-PROYECTO.supabase.co'

./mvnw spring-boot:run
# En otra terminal (con Docker activo):
./mvnw clean test
```

**Atención:** `POSTGRES_PASSWORD` en Compose y `DB_PASSWORD` en Spring deben contener la misma contraseña. El archivo `.env` no exporta automáticamente `DB_PASSWORD` para Java. No versionar tokens, contraseñas ni valores reales de `.env`.

## Índice documental

1. [Estado funcional y alcance](docs/estado-actual.md)
2. [Arquitectura y diseño](docs/arquitectura.md)
3. [Modelo de datos y migraciones](docs/base-de-datos.md)
4. [Contrato REST actual](docs/api.md)
5. [Autenticación y seguridad](docs/seguridad.md)
6. [Entorno y operación](docs/entorno-y-operacion.md)
7. [Pruebas y calidad](docs/pruebas.md)
8. [Decisiones y diferencias respecto al diseño v0.1](docs/decisiones.md)
9. [Riesgos y deuda técnica](docs/riesgos.md)
10. [Roadmap incremental](docs/roadmap.md)
11. [Cómo continuar y hacer entregas](docs/continuidad.md)
12. [Historial](CHANGELOG.md)

Incluye además ejemplos para importar en Postman en `postman/`.

## Regla de oro

**El código y las migraciones de la rama verificada definen lo que funciona hoy; los documentos de roadmap definen deseos, no endpoints existentes.** Cada cambio de comportamiento debe actualizar pruebas, documentación y API en el mismo PR.
