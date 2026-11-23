# AGENTS.md — Guía para futuros colaboradores y agentes de código

**Leer primero:** `README.md`, `docs/estado-actual.md`, `docs/arquitectura.md`, `docs/api.md`, `docs/base-de-datos.md`, `docs/riesgos.md` y `docs/continuidad.md`.

## Estado de partida

El backend ya existe en la **raíz Maven** del repositorio, paquete `com.financesuite.zsurapp`. No asumir la antigua estructura de monorepo `/backend` + `/frontend`; eso era una propuesta de 2026-11-21. El snapshot de referencia de FS-008 es `feat/transfers`; verificar `main` antes de desarrollar.

## Normas obligatorias

- Java 21, Spring Boot 4.2.0, arquitectura modular por dominio (`controller`, `dto`, `service`, `entity`, `repository`).
- Entregar cambios incrementales, con clases completas y pasos claros para Eclipse, Maven, Postman y Git.
- No exponer entidades JPA ni identificar usuario desde campos del JSON. Extraer `sub` del JWT válido y verificar `WorkspaceService.findByIdAndOwner` antes de acceder a recursos de ese espacio.
- Cada cuenta, categoría, transacción y transferencia debe estar ligada al Workspace autorizado. Probar accesos ajenos.
- `BigDecimal` + `NUMERIC(19,4)`, nunca `double` para dinero. Mantener el saldo inicial intacto y derivar saldo actual a partir de movimientos.
- Transferencia = **un registro en `transfers`** dentro de `@Transactional`, no un par de movimientos `transactions`. El ingreso y gasto del período no incluye transferencias.
- No permitir transferencias entre monedas distintas, cuentas idénticas o Workspaces distintos. El control de fondos disponibles **aún no está implementado**.
- Agregar/alterar tablas mediante una migración Flyway **nueva**. No editar V1–V6 ya aplicadas.
- Trabajar con DTOs de request/response y Bean Validation. Errores HTTP y reglas consistentes; no inventar rutas implementadas.
- Antes de cerrar: `./mvnw clean test`, `git diff --check`, revisar migraciones, pruebas negativas y documentación.
- No incluir secretos, credenciales, tokens o datos reales de usuarios en Git, documentación o screenshots.
- La existencia de `@Transactional` no equivale a protección contra doble envío o concurrencia. Evaluar idempotencia y bloqueos donde corresponda.

## Flujo de entrega

1. Confirmar rama `main` actualizada y comparar contra snapshot FS-008.
2. Escribir criterio de aceptación y riesgo de la nueva feature.
3. Crear rama `feat/...` a partir de `main` y ejecutar suite base.
4. Implementar por capas, con pruebas de servicio, seguridad HTTP y PostgreSQL cuando aplique.
5. Probar manualmente casos válidos y errores; no duplicar operaciones reales por descuido.
6. Actualizar `docs/api.md`, `docs/base-de-datos.md`, `docs/estado-actual.md`, `docs/roadmap.md`.
7. Ejecutar pruebas completas, commitear y abrir PR.

## Prioridad propuesta

Primero confirmar sincronización de `main`, luego robustecer idempotencia/concurrencia y saldos, mejorar seguridad/errores y trazabilidad; más adelante filtros, dashboard, frontend y despliegue. Ver `docs/roadmap.md`.
