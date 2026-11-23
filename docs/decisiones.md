# Decisiones arquitectónicas (ADR ligero)

**Estado:** lo que está implementado prevalece sobre propuestas del ZIP `finance-suite-docs-v2` (21/11/2026). Esta tabla conserva el porqué de las diferencias.

| ID | Decisión observada en código | Estado / motivo |
| --- | --- | --- |
| ADR-001 | Backend Maven en raíz; monolito modular Spring | Implementado; más simple que microservicios |
| ADR-002 | Identidad vía Supabase Auth JWT y propietario UUID en Workspace | Implementado; evita mantener passwords locales |
| ADR-003 | Workspaces `PERSONAL` y `BUSINESS`, un `owner_id` | Implementado; reemplaza por ahora la propuesta de `space_members` |
| ADR-004 | API `/api/v1/workspaces` | Implementado; el antiguo `/spaces` era solo conceptual |
| ADR-005 | `BigDecimal` / `NUMERIC(19,4)` | Implementado; el borrador decía `NUMERIC(19,2)` |
| ADR-006 | Fechas `Instant` / `TIMESTAMPTZ` | Implementado; el borrador proponía `DATE` |
| ADR-007 | Transacciones solo `INCOME` / `EXPENSE`; categoría obligatoria | Implementado; sin `TRANSFER_IN/OUT` en `transactions` |
| ADR-008 | Tabla independiente `transfers` con origen/destino e importe | Implementado; no existen dos asientos contables separados |
| ADR-009 | Balance derivado por suma, no columna mutable | Implementado; evita inconsistencias por desactualización de cache |
| ADR-010 | Autorización por propietario en servicio, no RLS Supabase automática | Implementado; PostgreSQL JDBC no recibe automáticamente contexto Auth |
| ADR-011 | UI React/TypeScript/PWA, Vercel/Render | **Objetivo histórico**, todavía no validado como desplegado |
| ADR-012 | Cada operación financiera es inmutable en la API actual (solo POST/GET) | **Estado actual**, no constituye aún política definitiva de anulación |

## Decisiones abiertas que deben resolverse antes de programar

1. **Fondos insuficientes:** ¿se prohíben sobregiros o se permiten por tipo de cuenta? ¿Saldo actual o saldo a fecha de operación?
2. **Concurrencia:** ¿bloqueos pesimistas por cuenta, ledger y serialización o saldo materializado verificable? Definir prueba de dos débitos simultáneos.
3. **Idempotencia:** clave para POST monetarios, ámbito por usuario/Workspace, expiración y respuesta repetida.
4. **Reversión/corrección:** operaciones de compensación auditables vs DELETE, historial de estado.
5. **Fechas:** zona por Workspace, fecha de negocio, gastos retroactivos, cierres de período.
6. **Colaboración:** membresías con roles (OWNER/EDITOR/VIEWER), migraciones desde `owner_id`, límites de privilegios.
7. **Multimoneda:** permitir cuentas en moneda diferente pero no transferir entre ellas sin FX; no sumar importes heterogéneos.
8. **Error estándar y versionado:** ProblemDetail consistente, códigos 409/422, cambios compatibles y API OpenAPI.
9. **Infraestructura:** base gestionada, backups, frontend y despliegue; aprobar diseño antes de implementarlo.

Registrar decisiones nuevas con fecha, alternativas consideradas, decisión, consecuencias, pruebas y migración si corresponde.
