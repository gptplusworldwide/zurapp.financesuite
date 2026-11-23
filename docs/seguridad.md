# Autenticación, autorización y seguridad

## Implementado

- Spring Security con `SecurityFilterChain`, sesiones `STATELESS` y OAuth2 Resource Server.
- Toda petición requiere autenticación, excepto preflight `OPTIONS /**` permitido.
- El recurso JWT está configurado con emisor y JWKS de Supabase Auth. Algoritmo `ES256`; audiencia `authenticated` declarada en `application.properties`.
- El `@AuthenticationPrincipal Jwt` proporciona `sub`; controllers lo convierten a `UUID`.
- `WorkspaceService.findByIdAndOwner(workspaceId, ownerId)` compara espacio y propietario; si no coincide, responde 404.
- Accesos a cuentas/categorías/operaciones se filtran por `workspace_id`, evitando revelar datos de otro Workspace al cliente autenticado.
- Pruebas de MVC y servicio ejercitan 401, 404 y otras validaciones negativas. **No equivale a pentest o auditoría exhaustiva**.

## Modelo de permisos actual

```text
Supabase Auth JWT.sub (UUID usuario)
  └── workspaces.owner_id (único propietario)
       ├── accounts
       ├── categories
       ├── transactions
       └── transfers
```

**No existe tabla de miembros ni roles funcionales** en las migraciones V1–V6. Las propuestas de roles `OWNER/EDITOR/VIEWER` pertenecen al roadmap histórico y no deben presentarse como autorizaciones disponibles.

## Límites y riesgos

1. **JDBC y RLS:** los filtros RLS de Supabase no protegen automáticamente una conexión backend PostgreSQL. Las operaciones JDBC se autorizan en los servicios; futuras consultas deben mantener esa protección.
2. **Integridad SQL multitenant:** las FKs individuales de `transactions` y `transfers` no impiden por sí solas insertar cuenta de otro Workspace si se usa SQL sin el servicio. Considerar FKs compuestas o triggers con una migración nueva.
3. **Logging DEBUG:** en configuración observada `logging.level.org.springframework.security=DEBUG` y `...oauth2=DEBUG`. Reducir a INFO/WARN en producción y no registrar tokens ni datos sensibles.
4. **CSRF:** desactivado por ser API stateless Bearer; si se adopta autenticación por cookies, revisar estrategia.
5. **CORS:** la regla `OPTIONS permitAll` no equivale a una política CORS segura para navegadores. Definir orígenes conocidos antes del frontend.
6. **Audiencia/emisor/reloj:** validar la configuración con tokens reales por entorno, expiración y rotación JWKS. Nunca deshabilitar validación para resolver un 401.
7. **Abuso:** no se observan rate limiting, protección ante reintentos de POST, auditoría, monitorización de errores ni políticas de recuperación.
8. **Cuentas multiusuario:** roles futuros requieren diseño de membresías, pruebas de aislamiento y evaluación de privilegios; no sustituir `owner_id` a la ligera.

## Checklist antes de desplegar

- [ ] Secretos solo en gestor de variables del entorno y nunca versionados.
- [ ] CORS restrictivo y HTTPS extremo a extremo.
- [ ] Logs en INFO/WARN, sin tokens ni identificadores sensibles.
- [ ] Reintentos y claves de idempotencia en operaciones monetarias.
- [ ] Fondos suficientes, política de sobregiro, límites y protección concurrente definidos.
- [ ] Backups restaurables y prueba de restauración.
- [ ] Usuarios A/B con pruebas cruzadas y tokens de expiración real.
- [ ] Errores HTTP coherentes sin fugas de existencia de recursos.
