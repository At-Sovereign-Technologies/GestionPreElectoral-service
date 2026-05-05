INSERT INTO gestion_pre_electoral.ciudadanos (
    id,
    tipo_documento,
    numero_documento,
    nombres,
    apellidos,
    fecha_nacimiento
) VALUES (
    1,
    'CC',
    '1000000001',
    'Ana',
    'Gomez',
    '1990-02-15'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO gestion_pre_electoral.registros_censo (
    eleccion_id,
    ciudadano_id,
    estado,
    causal_estado,
    observacion,
    actor_ultima_modificacion
) VALUES (
    1,
    1,
    'HABILITADO',
    NULL,
    'Registro semilla',
    'seed-script'
) ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

INSERT INTO gestion_pre_electoral.candidaturas (
    eleccion_id,
    nombre_candidato,
    documento,
    partido,
    circunscripcion,
    foto_url,
    estado,
    candidatura_reemplazada_id,
    motivo_reemplazo,
    justificacion_reemplazo,
    actor_ultima_modificacion
) VALUES (
    1,
    'Luis Perez',
    '900000001',
    'Partido Demo',
    'NACIONAL',
    NULL,
    'INSCRITA',
    NULL,
    NULL,
    NULL,
    'seed-script'
) ON CONFLICT (eleccion_id, documento) DO NOTHING;