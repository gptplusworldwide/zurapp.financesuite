# API REST implementada — v1

**Base local:** `http://localhost:8080` · **Prefijo:** `/api/v1` · **Identidad:** `Authorization: Bearer <access_token_de_Supabase>`.

> Esta es la API **en código** de `feat/transfers` (no la API conceptual del ZIP v0.1). Los campos en camelCase pertenecen al JSON. Todas las rutas, salvo que se indique, requieren JWT válido. `workspaceId` debe ser un UUID que pertenezca al `jwt.sub` autenticado.

## 1. Endpoints

| Método | Ruta relativa a `/api/v1` | Función |
| --- | --- | --- |
| GET | `/me` | Identidad UUID (`{"id":"..."}`) |
| POST | `/workspaces` | Crear Workspace |
| GET | `/workspaces` | Listar los propios |
| GET | `/workspaces/{workspaceId}` | Ver Workspace |
| PATCH | `/workspaces/{workspaceId}` | Renombrar |
| POST | `/workspaces/{workspaceId}/accounts` | Crear cuenta |
| GET | `/workspaces/{workspaceId}/accounts` | Listar cuentas |
| GET | `/workspaces/{workspaceId}/accounts/{accountId}` | Ver cuenta |
| PATCH | `/workspaces/{workspaceId}/accounts/{accountId}` | Renombrar cuenta |
| GET | `/workspaces/{workspaceId}/accounts/{accountId}/balance` | Balance derivado |
| POST | `/workspaces/{workspaceId}/categories` | Crear categoría |
| GET | `/workspaces/{workspaceId}/categories` | Listar categorías |
| GET | `/workspaces/{workspaceId}/categories/{categoryId}` | Ver categoría |
| PATCH | `/workspaces/{workspaceId}/categories/{categoryId}` | Renombrar categoría |
| POST | `/workspaces/{workspaceId}/transactions` | Crear ingreso/gasto |
| GET | `/workspaces/{workspaceId}/transactions` | Listar por fecha descendente |
| GET | `/workspaces/{workspaceId}/transactions/{transactionId}` | Ver movimiento |
| POST | `/workspaces/{workspaceId}/transfers` | Registrar transferencia |
| GET | `/workspaces/{workspaceId}/transfers` | Listar por fecha descendente |
| GET | `/workspaces/{workspaceId}/transfers/{transferId}` | Ver transferencia |

**No implementados:** `PUT`, `PATCH` o `DELETE` sobre transacciones/transferencias; listados con paginación o filtros; dashboard, reportes, budgets y OpenAPI publicado.

## 2. Requests de creación

### Workspace

```http
POST /api/v1/workspaces
Content-Type: application/json
```

```json
{"name":"Finanzas personales","type":"PERSONAL"}
```

Tipos: `PERSONAL`, `BUSINESS`. El propietario se deriva de JWT, no del body.

### Cuenta

```http
POST /api/v1/workspaces/{workspaceId}/accounts
```

```json
{"name":"Efectivo","type":"CASH","currency":"ARS","initialBalance":150000.0000}
```

Tipos: `CASH`, `BANK`, `DIGITAL_WALLET`. `currency` requiere tres letras mayúsculas. `initialBalance` usa hasta 15 dígitos enteros y 4 decimales.

### Categoría

```http
POST /api/v1/workspaces/{workspaceId}/categories
```

```json
{"name":"Supermercado","type":"EXPENSE"}
```

Tipos: `INCOME`, `EXPENSE`. Un mismo Workspace no admite dos categorías con el mismo `type` y `name`.

### Transacción

```http
POST /api/v1/workspaces/{workspaceId}/transactions
```

```json
{
  "accountId":"11111111-1111-4111-8111-111111111111",
  "categoryId":"22222222-2222-4222-8222-222222222222",
  "type":"EXPENSE",
  "amount":15000.0000,
  "description":"Compra del supermercado",
  "occurredAt":"2026-11-23T12:00:00Z"
}
```

Validación: account/category del Workspace, categoría y `type` coinciden; monto positivo con máximo 4 decimales, descripción hasta 500 caracteres, `occurredAt` UTC/ISO-8601. Se usan `Instant` y `TIMESTAMPTZ`, no `transactionDate` tipo DATE.

### Transferencia

```http
POST /api/v1/workspaces/{workspaceId}/transfers
```

```json
{
  "fromAccountId":"11111111-1111-4111-8111-111111111111",
  "toAccountId":"33333333-3333-4333-8333-333333333333",
  "amount":20000.0000,
  "description":"Transferencia al banco",
  "occurredAt":"2026-11-23T16:00:00Z"
}
```

Las cuentas tienen que ser distintas, del mismo Workspace y de la misma moneda. Sin validación actual de fondos suficientes. **Cada POST válido crea una nueva transferencia**, por lo que no hay que reintentar sin control.

## 3. Responses

- `POST`: `201 Created`, header `Location` con la URI del recurso y DTO JSON.
- `GET` de colección: `200 OK`, **array JSON simple**; sin wrapper ni paginación.
- `GET` de detalle y `PATCH`: `200 OK`, DTO.
- Respuesta `WorkspaceResponse`: `id,name,type,createdAt,updatedAt`.
- Respuesta `AccountResponse`: `id,workspaceId,name,type,currency,initialBalance,createdAt,updatedAt`.
- Respuesta `CategoryResponse`: `id,workspaceId,name,type,createdAt,updatedAt`.
- Respuesta `TransactionResponse`: `id,workspaceId,accountId,categoryId,type,amount,description,occurredAt,createdAt,updatedAt`.
- Respuesta `TransferResponse`: `id,workspaceId,fromAccountId,toAccountId,amount,description,occurredAt,createdAt,updatedAt`.
- `PATCH` Workspace/Account/Category solo recibe `{"name":"Nuevo nombre"}`; no modifica tipo, moneda, saldo ni otras propiedades.

### Balance por cuenta

```http
GET /api/v1/workspaces/{workspaceId}/accounts/{accountId}/balance
```

```json
{
  "accountId":"11111111-1111-4111-8111-111111111111",
  "workspaceId":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
  "name":"Efectivo",
  "currency":"ARS",
  "initialBalance":150000.0000,
  "totalIncome":0,
  "totalExpense":15000.0000,
  "incomingTransfers":0,
  "outgoingTransfers":20000.0000,
  "currentBalance":115000.0000
}
```

El saldo se calcula al consultar; no se almacena como columna `current_balance`.

## 4. Respuestas de error observadas en el código y pruebas

| HTTP | Causa típica |
| --- | --- |
| 400 | JSON inválido, validación Bean Validation, importe no positivo, distinta moneda, misma cuenta, categoría incorrecta |
| 401 | Token faltante, inválido o expirado |
| 404 | Workspace ajeno/no encontrado o recurso fuera del Workspace autorizado |
| 409 | Categoría duplicada al crear/renombrar |

No asumir un formato uniforme `ProblemDetail` para todos los casos: no se inspeccionó un manejador global de errores implementado. La UI cliente debe tratar de forma robusta distintos cuerpos de error.

## 5. Recomendaciones para siguientes versiones

No modificar contratos silenciosamente. Para filtros, paginación, reversión, idempotencia y fechas por zona de reporte, redactar primero casos de aceptación y actualizar esta referencia con el PR correspondiente.
