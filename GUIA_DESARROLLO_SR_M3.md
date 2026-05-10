# GUIA_DESARROLLO_SR_M3 — Lista Blanca (SR-M3)

Este documento explica cómo consumir y probar los endpoints de la `Lista Blanca` y el formato del payload para `modificar-emergencia`.

1) Endpoints relevantes

- `GET /api/v1/lista-blanca/metricas`
  - Respuesta: objeto con conteo total y listado por zona.
- `GET /api/v1/lista-blanca/verificar`
  - Respuesta: `{ "hashIntegridad": "<sha256>", "estado": "FIRMADA" }`.
- `PUT /api/v1/lista-blanca/modificar-emergencia`
  - Payload: ver sección siguiente.

2) Formato JSON para `modificar-emergencia`

Enviar un JSON con los campos siguientes (todos en español):

{
  "ciudadanoId": "<string-identificador-ciudadano>",
  "nuevaZona": "<nombre_zona>",
  "justificacion": "<texto claro de la razon del cambio>",
  "firmas": {
    "superadmin": { "usuario": "superadmin", "firma": "<firma-mock-base64>" },
    "cne": { "usuario": "cne", "firma": "<firma-mock-base64>" }
  }
}

- `ciudadanoId`: debe coincidir con el `ciudadano_id` existente en la tabla `lista_blanca`.
- `nuevaZona`: string con la nueva `zona_inscripcion`.
- `justificacion`: texto legible para auditoría.
- `firmas`: objeto JSON libre con las firmas multi-firmante; se almacenará tal cual en `lista_blanca_auditoria.firmas_json`.

3) Notas sobre integridad y trigger de protección

- La migración crea un trigger que lanza una excepción si la `eleccion` asociada tiene `estado = 'CERRADA'`. Esto simula la protección criptográfica habitual: no se permiten modificaciones cuando la elección está cerrada.
- El endpoint `modificar-emergencia` implementa una ruta de emergencia que, intencionalmente, desactiva triggers a nivel de sesión (`SET LOCAL session_replication_role = 'replica'`) para permitir el cambio y registrar la auditoría. Esto es un mecanismo de emergencia y debe usarse solo por personal autorizado.

4) Consumo por frontend

- Para `metricas`: consumir la ruta y pintar `totalCiudadanos` y el arreglo `porZona` con `zona` y `conteo`.
- Para `verificar`: mostrar `hashIntegridad` como prueba de integridad; actualizar cuando sea necesario.
- Para `modificar-emergencia`: el frontend debe presentar un formulario con `justificacion` y opciones para adjuntar firmas (pueden ser mocks en desarrollo). En producción, las firmas deben ser generadas con las claves de los actores autorizados.

5) Manejo de errores

- Si el trigger de base de datos impide la operación (elección CERRADA), la API devuelve HTTP 403 con mensaje "Operación prohibida: elección CERRADA".
- Errores de validación o negocio devuelven HTTP 400 con detalle en el campo `message`.

6) Ejemplo rápido (curl)

curl -X PUT -H "Content-Type: application/json" -d @payload.json http://localhost:8080/api/v1/lista-blanca/modificar-emergencia

Donde `payload.json` contiene el JSON de ejemplo de la sección 2.
