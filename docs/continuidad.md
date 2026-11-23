# Guía para retomar el desarrollo sin perder contexto

## Estado mínimo para iniciar una nueva sesión

**Proyecto:** Finance Suite / `zsurapp`. Backend Java 21, Spring Boot 4.2.0, Maven, PostgreSQL 17 en Docker `localhost:5433/finance_suite`, Flyway V1–V6 en la rama `feat/transfers`; Supabase Auth JWT. Cinco repositorios JPA y dominios `workspace`, `account`, `category`, `transaction`, `transfer`. Saldo derivado: inicial + ingresos − gastos + transferencias entrantes − salientes. Pruebas Maven **112 sin fallos ni errores reportadas por el usuario** el 23/11/2026. Próximo foco recomendado: idempotencia, sobregiros y concurrencia antes de producción.

## Paso 0 — Resolver Git antes de cambiar código

El desarrollador informó haber integrado `feat/transfers` en `main`, pero una lectura remota de GitHub en la elaboración de estos documentos mostró `main` con V1–V5 y `feat/transfers` con V1–V6. Revalidar:

```bash
git fetch origin --prune
git switch main
git pull --ff-only origin main
git log -1 --oneline
git ls-tree -r --name-only origin/main | grep 'V6__create_transfers.sql'
git ls-tree -r --name-only origin/main | grep 'transfer/repository/TransferRepository.java'
```

Si no aparecen los archivos, revisar la página del PR y si el merge se realizó en el repositorio correcto. **No hacer un merge ciego ni sobrescribir `main`**. Si `main` sí contiene V6, este documento debe marcar la discrepancia como resuelta. La lectura remota corresponde a los commits indicados en `docs/estado-actual.md`.

## Paso 1 — Preparar entorno

```bash
docker compose --env-file .env up -d
export DB_PASSWORD='***'
export SUPABASE_URL='https://TU-PROYECTO.supabase.co'
./mvnw clean test
```

No incluir valores secretos en resúmenes ni conversaciones compartidas.

## Paso 2 — Pruebas funcionales básicas

1. Token JWT vigente → `GET /api/v1/me` 200.
2. `GET /api/v1/workspaces` devuelve únicamente los propios.
3. Cuenta existente → GET `/balance` con cuatro componentes + inicial.
4. Transferencia válida entre dos cuentas ARS → POST 201, GET transferencia y saldos cambian por el mismo importe en direcciones opuestas.
5. Acceso sin token → 401; Workspace ajeno → 404.
6. No repetir POST monetarios solo para comprobar lectura: usar GET.

## Paso 3 — Planificar siguiente feature

1. Leer `docs/roadmap.md`, `docs/riesgos.md` y `docs/decisiones.md`.
2. Escribir contrato, reglas, casos negativos y definición de terminado antes de implementar.
3. Crear `feat/<nombre>` partiendo del `main` confirmado.
4. Mantener módulos existentes; crear migración Flyway incremental si cambia SQL.
5. Adaptar pruebas Maven, docs/API, README y changelog en la misma entrega.

## Plantilla de handoff para futuras conversaciones

```text
Proyecto: Finance Suite / zsurapp
Repo: https://github.com/gptplusworldwide/zurapp.financesuite
Rama actual: [completar]
Último commit local / origin/main: [completar]
Stack: Java 21, Spring Boot 4.2.0, Maven, PostgreSQL 17,
       Flyway, JPA, Supabase JWT, Docker y Testcontainers.
Módulos: workspaces, accounts+balances, categories, transactions, transfers.
Migraciones: V1–V6 en snapshot FS-008.
Tests: último reporte compartido 112/112 (2026-11-23); reejecutar.
Funcionalidades pendientes / riesgo: idempotencia, fondos suficientes,
       concurrencia, auditoría y dashboard/frontend.
Objetivo de esta sesión: [funcionalidad y criterio de aceptación]
Errores actuales: [si hay, con log relevante sin tokens]
Archivos tocados: [si aplica]
```

## Comandos de entrega

```bash
git status --short
git diff --check
./mvnw clean test
git add src/main src/test README.md AGENTS.md docs CHANGELOG.md postman
git diff --cached --check
git commit -m 'docs: record Finance Suite backend state and roadmap'
git push -u origin docs/current-state
```

Los comandos de add son **ejemplos**: adaptar si la nueva rama tiene otros archivos. Si una carpeta Postman no se desea versionar, omitirla del commit.
