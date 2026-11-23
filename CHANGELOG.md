# Changelog de Finance Suite

## Documentación — 2026-11-23

- Revisión del ZIP de documentación inicial del 2026-11-21.
- Actualización de contrato API a los endpoints actuales `/api/v1/workspaces`.
- Documentación del backend modular, migraciones V1–V6, entidad `transfers`, suma de saldos, seguridad y pruebas.
- Roadmap de crecimiento y riesgos de dinero (idempotencia, saldo suficiente y concurrencia).
- Registro de diferencia observada entre rama remota `main` y `feat/transfers`.

## Hitos funcionales informados en el desarrollo — 2026-11-23

| Feature | Descripción | Evidencia |
| --- | --- | --- |
| FS-001 | Infraestructura Spring/DB/Flyway | Proyecto y configuración revisados |
| FS-002 | Workspaces propietarios | Controller/service/V2 |
| FS-003 | Cuentas | Controller/service/V3 |
| FS-004 | Categorías | Controller/service/V4 |
| FS-006 | Ingresos/gastos | Controller/service/V5 |
| FS-007 | Saldos por cuenta | AccountBalanceController/Service |
| FS-008 | Transferencias y balance con entrada/salida | Rama `feat/transfers`, V6, controller/service, tests |

**Suite reportada:** 112 tests sin fallos, errores ni omitidas. No equivale a verificación de CI ni a despliegue.

## Diseño inicial — 2026-11-21

Documentación conceptual de suite PWA con módulos React, Spaces, membresías, dashboard, Render/Vercel y base en Supabase. Varias decisiones evolucionaron durante la implementación y están documentadas en `docs/decisiones.md`.
