# GestionPreElectoral-service — Entrega a Desarrolladores

## Visión General
Microservicio para gestión pre-electoral: censo de ciudadanos, candidaturas, sorteo de jurados, actas de votación, y el subsistema de seguridad **MFA + Bóveda Air-Gap** (M7-02 / M7-03).

**Stack Tecnológico:** Spring Boot 3.x, Java 21, Hibernate 7, Flyway, PostgreSQL 15, gRPC (hacia ConfiguracionEleccion-service).

---

## Variables de Entorno

| Variable | Requerida | Default | Descripción |
|----------|-----------|---------|-------------|
| `DB_URL` | Sí | — | URL JDBC: `jdbc:postgresql://host:5432/gestion_pre_electoral` |
| `DB_USUARIO` | Sí | — | Usuario DB |
| `DB_CLAVE` | Sí | — | Contraseña DB |
| `PUERTO` | No | `8082` | Puerto servidor HTTP |
| `ELECCIONES_GRPC_HOST` | Sí | — | Host gRPC para ConfiguracionEleccion |
| `ELECCIONES_GRPC_PORT` | No | `9090` | Puerto gRPC |

**Ejemplo (docker-compose):**
```yaml
environment:
  DB_URL: jdbc:postgresql://gestion-pre-electoral-postgres:5432/gestion_pre_electoral
  DB_USUARIO: gestion_pre_electoral
  DB_CLAVE: gestion_pre_electoral
  PUERTO: 8082
  ELECCIONES_GRPC_HOST: configuracion-eleccion
  ELECCIONES_GRPC_PORT: 9090
```

---

## Base de Datos

### Esquema
```
gestion_pre_electoral
├── lista_blanca           (+ cols mfa_*, col rol)
├── clavero_key_record     (V13)
├── ceremony_session       (V13)
├── ciudadanos, candidaturas, jurados_mesa, acta_e14, ...
```

### Migraciones
Flyway corre al iniciar. Versión actual: **V13**.

Migraciones clave:
- `V12__add_mfa_fields_to_lista_blanca.sql` — Columnas MFA + sembrado de hashes de contraseña
- `V13__crear_tablas_boveda_ceremonia.sql` — Tablas de bóveda + 3 MAGISTRADOS clavero simulados

**Migraciones manuales fueron aplicadas fuera de Flyway** y luego reaplicadas por Flyway. El bloque de seed usa `DO $$` con `RETURNING` — duplicados son un riesgo conocido si se re-ejecuta.

---

## Contratos API

### Autenticación

#### `POST /api/v1/auth/login`
**Request:**
```json
{
  "numeroDocumento": "110000001",
  "contrasena": "password123"
}
```

**Respuestas:**

| Status | Body | Escenario |
|--------|------|-----------|
| 200 | `{"status":"AUTHENTICATED","token":"<session_jwt>","user":{...}}` | CIUDADANO (sin MFA) |
| 200 | `{"status":"MFA_SETUP_REQUIRED","token":"<temp_jwt>","user":{...}}` | MAGISTRADO, MFA no configurado |
| 200 | `{"status":"MFA_CHALLENGE","token":"<temp_jwt>","user":{...}}` | MAGISTRADO, MFA configurado |
| 400 | `{"message":"Documento y contrasena requeridos"}` | Campos faltantes |
| 401 | `{"message":"Credenciales invalidas"}` | Documento o contraseña incorrectos |

#### `POST /api/v1/auth/mfa/setup`
**Auth:** Bearer token (temporal o de sesión aceptados)

**Respuesta:**
```json
{
  "qrCodeUrl": "mock://totp/setup",
  "secret": "MOCK_TOTP_SECRET",
  "message": "TOTP setup mocked..."
}
```

#### `POST /api/v1/auth/mfa/verify`
**Auth:** Bearer token (temporal o de sesión aceptados)
**Request:** `{"otpCode":"123456"}`

**Respuesta:**
```json
{
  "status": "MFA_VERIFIED",
  "token": "<session_jwt>"
}
```

#### `GET /api/v1/auth/me`
**Auth:** Bearer token de sesión
**Respuesta:** Perfil de usuario JSON (numeroDocumento, nombre, telefono, correo, rol, mfaEnabled)

### Bóveda / Ceremonia

#### `GET /api/v1/vault/status`
**Auth:** Bearer token (sesión, mfa_verified=true)
**Header:** `X-Vault-Shard: <shard_value>`

**Respuesta (shard simulado):**
```json
{"status":"VAULT_ONLINE","vault_access":true,"message":"Boveda accesible..."}
```

**Respuesta (sin ceremonia / shard inválido):**
```json
{"error":"No hay una ceremonia activa o la clave no es válida..."}
```

#### `POST /api/v1/ceremony/initiate`
**Auth:** Bearer token de sesión
**Request:** `{"type":"APERTURA"}`

**Respuesta:**
```json
{
  "ceremonyId": "<uuid>",
  "status": "PENDING",
  "requiredShards": 3,
  "submittedShards": 0,
  "expiresAt": "2026-..."
}
```

#### `POST /api/v1/ceremony/{id}/submit-shard`
**Auth:** Bearer token de sesión
**Request:** `{"shard_value":"MOCK_SHARD_INDEX_1_VALUE"}`

**Respuesta:** Estado actualizado de la ceremonia

#### `GET /api/v1/ceremony/{id}/status`
**Respuesta:** Estado completo de la ceremonia

#### `POST /api/v1/ceremony/{id}/abort`
**Auth:** Bearer token de sesión

---

## Notas de Arquitectura

### Controladores de Auth Duales
Dos controladores coexisten por diseño:
1. **`ControladorAuth`** (`/api/v1/auth`) — flujo nuevo consciente de MFA
2. **`ControladorAuthMock`** (`/api/v1/auth-mock`) — flujo OTP legacy (preservado para compatibilidad con frontend)

El controlador mock fue renombrado de `/api/v1/auth` a `/api/v1/auth-mock` para evitar conflictos de ruteo en Spring.

### Tipos de Token

| Tipo | Claim | TTL | Uso |
|------|-------|-----|-----|
| `temp` | `type: "temp"`, `status: "MFA_*"` | 5 min | Token interino durante compuerta MFA |
| `session` | `type: "session"`, `mfa_verified: true` | 1 hora | Sesión autenticada completa |

Todos los tokens contienen `vault_access: false` (hardcodeado).

### Validación de Contraseña
```java
private boolean validarContrasena(String raw, String hash) {
    if (hash == null || hash.isBlank()) {
        return raw.equals("password123");
    }
    return hash.equals("<bcrypt_hash>") && raw.equals("password123");
}
```
- El hash bcrypt `$2a$10$N9qo8uLOickgx2ZMRZoMye9jT/.rGgNLGR0nV1cX7U0pW8kXj6aJGe` fue sembrado para todos los usuarios
- Sin comparación bcrypt real — solo verificación de igualdad de strings (mock)

### Flujo de Compuerta MFA
```
login(numeroDocumento, contrasena)
  → buscar usuario
  → validar contraseña
  → requiresMFA(rol) ?
      false → retornar token de sesión
      true  → mfaEnabled ?
                true  → retornar MFA_CHALLENGE + token temporal
                false → retornar MFA_SETUP_REQUIRED + token temporal
```

### Modelo de Seguridad Air-Gap
Los tokens de sesión web otorgan **cero** acceso a la bóveda. La bóveda requiere:
1. Sesión web válida (Plano A)
2. Header `X-Vault-Shard` (Plano B)
3. Sesión de ceremonia activa

Los shards simulados omiten la verificación de ceremonia para propósitos de demo.

### VaultAccessGuard
- Requiere `@Transactional(readOnly = true)` (proxies lazy)
- La verificación de shard simulado (`MOCK_SHARD_INDEX_*`) corre **antes** del loop de huellas de clavero reales
- Shards reales requieren `ceremony.status == ACTIVE && !expired`

### Gotchas Transaccionales
- Se removió `@Transactional` de los métodos del controlador `ControladorBovedaCeremonia`
- Se mantuvo `@Transactional` en los métodos del servicio `ServicioBoveda`
- Mezclar transacciones de controlador + servicio causaba `UnexpectedRollbackException`

### Baseline de Flyway
La DB fue modificada manualmente antes del baseline de Flyway. Si alguna vez se resetea la DB:
1. V12 agrega columnas + siembra contraseñas
2. V13 crea tablas + siembra 3 MAGISTRADOS clavero
3. La columna `rol` fue agregada manualmente fuera de las migraciones

### Issues Conocidos
- **Usuarios duplicados:** El bloque de seed de V13 puede crear duplicados si se re-ejecuta. Fix: `numero_documento` debería tener constraint UNIQUE
- **Sin filtro Spring Security:** Los endpoints están abiertos. `isAuthenticated()` es manual por controlador
- **TOTP simulado:** Cualquier código de 6 dígitos pasa validación
- **LazyInitializationException:** Fix agregando `@Transactional(readOnly = true)` a `VaultAccessGuard.hasVaultAccess()`

---

## Integración Frontend

### Patrón de Cliente API
```typescript
const API_URL =
    trim(import.meta.env.VITE_AUTH_SERVICE_URL) ||
    trim(import.meta.env.VITE_API_URL) ||
    trim(import.meta.env.VITE_API_GATEWAY_URL) ||
    "";
```
- String vacío = mismo origen (funciona vía proxy del gateway en puerto 8091)
- Acceso directo al puerto 8082 funciona para pruebas locales

### Pestañas de Página de Perfil
- **Perfil:** Visible para todos los roles
- **Seguridad MFA:** Oculto para `CIUDADANO`
- **Mis Claves:** Visible para todos los roles

---

## Build y Deploy

```bash
# Backend
cd GestionPreElectoral-service
docker build -t gestion-pre-electoral:test .

# Stack completo
docker compose -f docker-compose.local.yml up -d --build
```

---

## Referencia Rápida de Pruebas

Ver `/.agent/testing/` para procedimientos completos de pruebas manuales:
- `auth-login.md` — Flujos de login, compuertas MFA
- `mfa-setup.md` — Configuración/verificación TOTP
- `boveda-ceremonia.md` — Acceso a bóveda, ceremonia de shards
- `profile-page.md` — Navegación de página de perfil

## Datos de Prueba
- 50 usuarios CIUDADANO (documentos 110000001–110000050)
- 3 MAGISTRADOS clavero (99999001–99999003)
- Todas las contraseñas: `password123`
