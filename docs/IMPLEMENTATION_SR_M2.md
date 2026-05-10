# IMPLEMENTATION_SR_M2

## Resumen
Esta implementacion cubre dos historias de usuario del modulo SR-M2 con un enfoque pragmatico:

- El censo se congela con logica real en Java y un bloqueo real en PostgreSQL.
- Los jurados de mesa se sincronizan y la apertura de mesa exige un minimo de 2 tokens validos asociados a la misma mesa.

## 1) US-SR-M2-01 - Ingesta y Congelamiento Criptografico del Censo

### Nuevo endpoint

`POST /api/censo/elecciones/{eleccionId}/congelar`

### JSON de entrada

```json
{
  "actor": "superadmin-sistema"
}
```

### JSON de salida

```json
{
  "eleccionId": 1,
  "estado": "CERRADA",
  "hashRaiz": "a8f3b1e5f8f4b9c2e7d8a2b1f0c4d5e6f7890abc1234567890abcdef12345678",
  "totalRegistros": 125000
}
```

### Que hace el backend

- Valida por gRPC que la eleccion este cerrada.
- Lee todos los valores `hash_biometrico` de `gestion_pre_electoral.registros_censo` para la eleccion indicada.
- Ordena la lista por `id` a nivel de consulta y calcula un hash SHA-256 unico sobre la concatenacion de todos los hashes biometricos.
- Guarda el resultado en `gestion_pre_electoral.estado_congelamiento_censo`.
- Registra un evento de auditoria con tipo `CENSO_CONGELADO`.

### Como funciona el hash raiz simplificado

No se implementa un arbol de Merkle completo. En su lugar:

1. Se toman todos los `hash_biometrico` de la eleccion.
2. Se concatenan en orden estable.
3. Se aplica un solo SHA-256 al texto resultante.

Eso da una huella raiz simple y deterministica para fines academicos y de integracion.

### Restriccion fisica en PostgreSQL

La migracion `V7__trigger_congelamiento_censo.sql` crea:

- La tabla `gestion_pre_electoral.estado_congelamiento_censo`.
- La columna `hash_biometrico` en `gestion_pre_electoral.registros_censo`.
- Un trigger `BEFORE INSERT OR UPDATE OR DELETE` sobre `registros_censo`.

Regla del trigger:

- Si la eleccion esta marcada como `CERRADA` o `censo_congelado = true`, PostgreSQL lanza una excepcion y bloquea la modificacion.

Eso significa que, aunque una llamada Java intente escribir, la base de datos tambien la rechaza.

## 2) US-SR-M2-04 - Sincronizacion de Jurados y M-of-N Authorization

### Nuevo endpoint de sincronizacion

`POST /api/v1/jurados/sincronizar`

### JSON de entrada

Se envia una lista JSON de jurados.

```json
[
  {
    "nombre": "Maria Torres",
    "documento": "1002003001",
    "mesaId": 15,
    "rol": "PRESIDENTE"
  },
  {
    "nombre": "Juan Perez",
    "documento": "1002003002",
    "mesaId": 15,
    "rol": "VICEPRESIDENTE"
  }
]
```

### JSON de salida

```json
[
  {
    "id": 1,
    "nombre": "Maria Torres",
    "documento": "1002003001",
    "mesaId": 15,
    "rol": "PRESIDENTE",
    "tokenAcceso": "2d5a8b2c-29f2-4fb4-9a6e-7d0d89f6d6f8"
  },
  {
    "id": 2,
    "nombre": "Juan Perez",
    "documento": "1002003002",
    "mesaId": 15,
    "rol": "VICEPRESIDENTE",
    "tokenAcceso": "6fcd5b55-67f2-4d9a-8d4d-7d5c7f1a0f13"
  }
]
```

### Nuevo endpoint de apertura de mesa

`POST /api/v1/mesas/{mesaId}/apertura`

### JSON de entrada

```json
{
  "tokens": [
    "2d5a8b2c-29f2-4fb4-9a6e-7d0d89f6d6f8",
    "6fcd5b55-67f2-4d9a-8d4d-7d5c7f1a0f13"
  ]
}
```

### JSON de salida

```json
{
  "estado": "ABIERTA",
  "formularios": ["E-11", "E-9"]
}
```

### Regla M-of-N

El servicio valida lo siguiente:

- Debe haber al menos 2 tokens.
- Todos los tokens enviados deben pertenecer a jurados asignados a la `mesaId` de la URL.
- Si falta alguno, se lanza una excepcion 403.

### Excepcion 403

Cuando falla la validacion de apertura, el backend responde con `403 Forbidden` mediante la excepcion `ExcepcionAccesoDenegado`.

## 3) Como debe llamar el frontend el endpoint M-of-N

1. El frontend debe guardar los `tokenAcceso` entregados por `/api/v1/jurados/sincronizar`.
2. Cuando la mesa de votacion deba abrirse, debe recolectar al menos dos tokens distintos.
3. Debe hacer un `POST` a `/api/v1/mesas/{mesaId}/apertura` enviando un body JSON con la propiedad `tokens`.
4. Si la respuesta es `200 OK`, la mesa queda habilitada para continuar el flujo.
5. Si la respuesta es `403`, el frontend debe mostrar que la autorizacion no es valida para esa mesa.

## 4) Notas de implementacion

- Los tokens de jurado se simulan con `UUID.randomUUID().toString()`.
- No se usa PKI real ni JWT real.
- El root hash del censo es una simulacion pragmatica, no un arbol de Merkle completo.
- El bloqueo del censo es doble: logica de servicio y trigger real en PostgreSQL.

## 5) Cambios de base de datos

- `V7__trigger_congelamiento_censo.sql`
  - crea `estado_congelamiento_censo`
  - agrega `hash_biometrico` a `registros_censo`
  - crea el trigger de bloqueo
  - backfillea `hash_biometrico` para registros existentes

- `V8__create_jurado_mesa_table.sql`
  - crea `jurados_mesa`
  - agrega restricciones unicas e indices utiles
