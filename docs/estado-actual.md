# Estado funcional real de Finance Suite

**Corte:** 2026-11-23. **Fuentes:** código de `feat/transfers` inspeccionado en GitHub + ejecución de pruebas y validaciones manuales reportadas en la conversación.

## Módulos terminados

| Iteración | Componente | Disponible |
| --- | --- | --- |
| FS-001 | Base técnica Java/Spring/PostgreSQL/Flyway | Backend, Compose, tests básicos |
| FS-002 | Workspaces | POST, GET, GET por ID, PATCH nombre |
| FS-003 | Accounts | POST, GET, GET por ID, PATCH nombre |
| FS-004 | Categories | POST, GET, GET por ID, PATCH nombre |
| FS-006 | Transactions | POST, GET, GET por ID; `INCOME`/`EXPENSE` |
| FS-007 | Account balances | GET `/accounts/{accountId}/balance` |
| FS-008 | Transfers | POST, GET, GET por ID; integradas al saldo |

El número FS-005 no está documentado como entrega independiente en los mensajes revisados. No atribuirle funcionalidades sin evidencia.

## Capacidades operativas

1. Identidad validada con JWT de Supabase; el usuario se identifica por `jwt.sub` UUID.
2. Workspace con tipo `PERSONAL` o `BUSINESS` y `owner_id`.
3. Cuentas `CASH`, `BANK`, `DIGITAL_WALLET`, con moneda ISO-like de tres mayúsculas y `initialBalance`.
4. Categorías `INCOME`/`EXPENSE` no duplicadas por `(workspace_id, type, name)`.
5. Movimientos de ingreso/gasto con importe positivo, categoría del mismo espacio y del tipo correspondiente.
6. Transferencias con un solo registro, dos cuentas distintas dentro del mismo Workspace y de la misma moneda.
7. Saldos por cuenta derivados en tiempo de consulta; muestran ingresos, gastos, transferencias recibidas/enviadas.
8. Pruebas automatizadas de servicio, controlador protegido y repositorio usando PostgreSQL temporal.

## No implementado o no verificado en el snapshot

- Endpoints para editar, anular o borrar transacciones/transferencias.
- Filtros por período/cuenta/categoría y paginación en listados financieros.
- API agregada para dashboard por Workspace, presupuestos, reportes o exportación.
- Frontend React, PWA instalable, login web ni despliegue Vercel/Render en este repositorio inspeccionado.
- Colaboradores/roles OWNER–EDITOR–VIEWER (actualmente la autorización se basa en `workspaces.owner_id`).
- Validación de saldo disponible, bloqueo/serialización frente a transferencias concurrentes, idempotencia de POST.
- Conversión de monedas, transferencias entre espacios, integración bancaria o auditoría inmutable.
- Política de reversión/cancelación financiera y zona horaria de reportes.

## Prueba funcional manual confirmada por el usuario

Un Workspace personal tuvo una cuenta con saldo inicial 150.000 ARS; se registró gasto de 15.000 ARS y luego transferencia de 20.000 ARS hacia una cuenta bancaria inicialmente en cero. En las respuestas de API reportadas:

- Cuenta de origen: `initialBalance=150000`, `totalExpense=15000`, `outgoingTransfers=20000`, `currentBalance=115000`.
- Cuenta de destino: saldo previo `0`, transferencia recibida `20000`, saldo `20000`.
- Suma de ambas cuentas: 135.000 ARS, sin creación de ingresos ficticios por la transferencia.

Estos son **datos de prueba locales**, no deben utilizarse como datos semilla de producción ni incluir UUID personales en ejemplos compartidos.

## Calidad observada

Última ejecución Maven **reportada por el usuario** el 23/11/2026: `Tests run: 112, Failures: 0, Errors: 0, Skipped: 0`. No se ejecutó la suite dentro de esta tarea de documentación. Ver `docs/pruebas.md`.

## Revisión de la rama principal antes del próximo sprint

La consulta de GitHub realizada al generar este paquete mostró:

- `main`: commit `7783e7ce801fbccddd82d1db4608c0373ac8b3b9`, migrations hasta **V5**, sin módulo `transfer`.
- `feat/transfers`: commit `955ed198bd626881e7e973b8ebf1b2fa29db83c7`, migrations **V1–V6**, módulo transfer completo.

Aunque el desarrollador reportó que FS-008 se integró a `main`, **esa integración no pudo verificarse mediante la lectura remota realizada**. Podría existir retardo de sincronización o un estado distinto de repo/branch. No concluir que la integración falló sin verificarlo de nuevo. Revisar `docs/continuidad.md`.
