# Estrategia de pruebas y calidad

## Resultado más reciente reportado

En la conversación del **23/11/2026**, después del ajuste de `AccountBalanceResponse`, el desarrollador ejecutó `./mvnw clean test` y compartió:

```text
Tests run: 112, Failures: 0, Errors: 0, Skipped: 0
```

**Origen:** salida local compartida por el usuario. No se reprodujo la suite ni se verificó el renglón `BUILD SUCCESS` dentro de este proceso de documentación. El balance de pruebas actual es un registro histórico y puede variar al cambiar el código.

## Tipos cubiertos

| Capa | Mecanismo | Ejemplos |
| --- | --- | --- |
| Service | JUnit 5 + Mockito | Propietario, validaciones monetarias, moneda, categorías, saldos |
| HTTP + seguridad | MockMvc, `@WebMvcTest`, JWT simulado | 401 sin token, 201 válido, 400 DTO, 404 ajeno |
| Repository | PostgreSQL 17-alpine Testcontainers, JPA, JDBC | FKs, persistencia, orden, sumas, aislamiento por Workspace |
| Bootstrap | `ZsurappApplicationTests` | Carga del contexto de Spring |

Archivos principales: `Workspace*Test`, `Account*Test`, `Category*Test`, `Transaction*Test`, `TransferServiceTest`, `TransferControllerSecurityTest`, `TransferRepositoryTest`, `AccountBalanceServiceTest`.

## Escenarios críticos ya ejercitados

- JWT ausente → 401.
- Workspace ajeno → 404; servicio no accede a otros repositorios.
- Cuentas/categorías fuera de Workspace → rechazo.
- Ingreso y gasto separados; suma en `BigDecimal`.
- Categoría no corresponde al tipo del movimiento → rechazo.
- Transferencia entre misma cuenta, moneda distinta, importe cero/negativo, fecha nula → rechazo.
- Saldos iniciales inmutables ante transacciones y transferencias.
- Transferencias entrantes suman; salientes restan.
- Filtros por Workspace para consultas de saldo.

## Pendientes de prueba antes de producción

- Idempotencia de POST y retries (dos solicitudes idénticas simultáneas no deben duplicar dinero si así se diseña).
- Concurrencia real para fondos suficientes; transacciones simultáneas y aislamiento DB.
- Transferencias con importes que exceden saldo y política de sobregiro.
- Campos inválidos/límites extremos (`NUMERIC(19,4)`, precision/scale, fechas), y conversión de errores de BD.
- Integridad SQL contra referencias cruzadas de Workspace por un cliente alternativo al servicio.
- Pruebas end-to-end navegador, cambios de entorno, backup/restauración, seguridad de despliegue.
- Test de regresión para contrato de response y datos de transferencias.

## Comandos

```bash
./mvnw clean test
./mvnw test -Dtest=AccountBalanceServiceTest
./mvnw test -Dtest=TransferControllerSecurityTest
./mvnw test -Dtest=TransferRepositoryTest

git diff --check
```

No declarar un sprint terminado hasta confirmar resultado real y verificar también el código de salida Maven. Si cambia el DTO `AccountBalanceResponse`, actualizar las expectativas en `AccountBalanceControllerSecurityTest` y los cálculos comparados.
