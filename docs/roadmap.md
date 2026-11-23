# Roadmap de evolución — de backend funcional a Finance Suite

**Corte:** 2026-11-23. Las estimaciones de alcance son prioridades recomendadas, no compromisos de fecha.

## Etapas ya implementadas

- [x] FS-001: fundamento de backend, PostgreSQL, Flyway y Maven.
- [x] FS-002: Workspaces personales/empresariales con propietario.
- [x] FS-003: cuentas financieras.
- [x] FS-004: categorías ingreso/gasto.
- [x] FS-006: transacciones de ingreso/gasto.
- [x] FS-007: saldo derivado por cuenta.
- [x] FS-008: transferencias con impacto en saldos, tests reportados (rama `feat/transfers`).
- [ ] Verificar presencia de FS-008 en `origin/main` (desfase observado en lectura remota).

## Fase A — Fiabilidad financiera (recomendación: primero)

### FS-009 — Protección de transferencias y reintentos [P0]

**Alcance:** definir política de sobregiros; prevenir duplicados por reintento de red; proteger operaciones concurrentes. Considerar claves de idempotencia, índices únicos, bloqueo/coordinación y tests multi-hilo con PostgreSQL. No alterar `initial_balance`.

**Aceptación:** dos POST idénticos con misma clave no generan dos cargos; dos transferencias concurrentes no vulneran la política de fondos; rollback correcto; 400/409/422 definidos; sin acceso cruzado.

### FS-010 — Integridad y observabilidad [P1]

**Alcance:** cerrar inconsistencias entre workspace/cuenta/categoría al nivel adecuado, errores JSON consistentes, logs seguros, corrección de código scaffold, métricas básicas y reglas CORS.

**Aceptación:** SQL inválido no puede cruzar tenants; tests negativos nuevos; API devuelve formato de error documentado; logging productivo no filtra credenciales.

### FS-011 — Reversión y auditoría de movimientos [P1]

**Alcance:** modelo de cancelación/compensación e historial; no editar destructivamente transferencias monetarias ya contabilizadas.

**Aceptación:** anular revierte saldos exactamente una vez, conserva referencia al original y soporta reintentos.

## Fase B — Experiencia financiera del usuario

### FS-012 — Consultas y filtros [P1]

Filtrar movimientos por rango `from/to`, cuenta, categoría y tipo; paginación `page/size`, orden estable por fecha e ID; adaptar índices y contrato API. Aceptación: no se mezclan Workspaces; resultado determinístico y seguro bajo datos grandes.

### FS-013 — Dashboard / resumen por período [P1]

Ingresos, gastos, resultado neto y ranking por categoría/rango. Excluir transferencias de los ingresos/gastos y separar variación de saldo de resultado del período. Aceptación: totales cuadran con los movimientos y con la zona horaria documentada.

### FS-014 — Frontend React + TypeScript + Vite [P2]

Crear módulo web con Supabase Auth, selección de Workspaces, cuentas, saldos, movimientos y transferencias. Diseño responsive. Aceptación: flujo principal sin Postman y errores de red representados correctamente.

### FS-015 — PWA y despliegue [P2]

Service worker con política segura (no cachear datos financieros autenticados sin diseño explícito), entornos separados, OpenAPI, CI/CD, backups/restore, staging y despliegue. Aceptación: instalación móvil, pruebas E2E y recuperación documentada.

## Fase C — Extensión de producto

- [ ] Presupuestos por categoría/mes, alertas y tendencias.
- [ ] Pagos recurrentes, tarjeta de crédito, cuotas.
- [ ] Exportar CSV/Excel, reportes y conciliación.
- [ ] Espacios compartidos con roles y membresías.
- [ ] Multimoneda con cotizaciones y reglas contables.
- [ ] Módulos adicionales de la suite (tareas/objetivos) sin acoplar las finanzas a otros productos.

## Disciplina por sprint

Una feature por rama; API y migración solo después de decidir el comportamiento. Incluir unitarias, pruebas HTTP de seguridad y repositorio cuando corresponda; actualizar docs y CHANGELOG en el PR. El cierre exige evidencia real de tests, Postman cuando aplique y revisión de riesgos.
