# Riesgos conocidos y deuda técnica

**Lectura:** prioridades propuestas para desarrollo; *no* son afirmaciones de incidentes ya ocurridos.

| Prioridad | Riesgo | Evidencia actual | Mitigación / aceptación futura |
| --- | --- | --- | --- |
| P0 | Rama `main` potencialmente desactualizada | GitHub remoto leído muestra V1–V5; `feat/transfers` V1–V6 | Confirmar SHA remoto y presencia de V6 en `origin/main` antes de crecer |
| P0 | Doble registro en reintentos HTTP | POST monetarios carecen de clave de idempotencia | Idempotency key única por ámbito y prueba de repetición |
| P0 | Fondos insuficientes | `TransferService.create` no consulta saldo ni impone límite | Especificar sobregiro; aplicar verificación atómica con concurrencia |
| P0 | Carrera de dos transferencias simultáneas | `@Transactional` solo guarda un registro; no serializa saldo | Pruebas concurrentes, bloqueo/aislamiento adecuados |
| P1 | Integridad multi-tenant solo en lógica Java | V5/V6 FKs simples no vinculan account/category al workspace | Nueva migración con FKs compuestas/trigger o validación SQL equivalente |
| P1 | Sin historial de correcciones y reversión | No existen PATCH/DELETE sobre movimientos/transferencias | Diseñar reversión inmutable y auditable, prohibir borrado destructivo |
| P1 | Errores no normalizados | Sin handler global comprobado | Excepciones de dominio y respuestas ProblemDetail consistentes |
| P1 | Logging sensible de seguridad | DEBUG para Spring Security en properties | Logging INFO/WARN y tratamiento seguro de datos personales |
| P1 | Sin filtros/paginación | `findAllByWorkspace...` devuelve toda la colección | Pageable, filtros, orden estable e índices compuestos |
| P1 | Despliegue/backup no probado | Solo entorno local demostrado | Plan de operación, CI, backup y prueba de restauración |
| P2 | Sin frontend/PWA | No aparece frontend en rama inspeccionada | React/TS/Vite, login, selector de Workspaces y UI de Finanzas |
| P2 | Sin dashboard ni analítica temporal | Solo saldo por cuenta y listados | Agregación por rango, zona horaria y categoría |
| P2 | Expansión futura de permisos | Solo `owner_id` | Roles/membresías con migración y pruebas negativas |
| P2 | Restos de scaffold en código | Archivos `account/accountt.java`, `transaction/trans.java` en árbol | Revisar contenido y eliminar si realmente obsoletos, mediante PR probado |

## Nota especial sobre dinero

El cálculo actual admite que `currentBalance` sea negativo. No bloquear por defecto sin definir si las cuentas `BANK`, `CASH` o `DIGITAL_WALLET` permiten sobregiro. Al diseñar límites, contemplar operaciones retroactivas, transferencias simultáneas, rechazos de pago, ajustes y error de red luego de un commit exitoso.

## Criterio de preparación para producción

No declarar producción lista únicamente por 112 tests: exigir validación completa de concurrencia, idempotencia, autorización, migraciones, monitoreo, secretos, backups/restore, HTTPS/CORS y funcionamiento de cliente. La app actual es una base funcional de desarrollo.
