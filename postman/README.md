# Postman — usar la API local

1. Importar `FinanceSuite-API.postman_collection.json` y `FinanceSuite-Local.postman_environment.json` en Postman.
2. Seleccionar el entorno **Finance Suite Local**.
3. Completar `accessToken` con un JWT válido de Supabase sin guardarlo en Git ni exportarlo en archivos compartidos.
4. Crear o consultar un Workspace y copiar su `id` a `workspaceId`.
5. Crear/consultar cuentas y categorías, configurar `accountId`, `destinationAccountId`, `categoryId`.
6. Ejecutar GET seguros antes de operaciones monetarias. Los valores de importes y fechas son **ejemplos**.
7. La creación mediante POST no es idempotente: cada envío válido registra una nueva operación.

Esta colección representa los endpoints inspeccionados de la rama `feat/transfers`; no reemplaza el contrato completo de `docs/api.md`.
