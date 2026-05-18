-- Insert seed ciudadano (safe, no constraint issues)
INSERT INTO gestion_pre_electoral.ciudadanos (
    id,
    tipo_documento,
    numero_documento,
    nombres,
    apellidos,
    fecha_nacimiento
)
SELECT
    1,
    'CC',
    '1000000001',
    'Ana',
    'Gomez',
    '1990-02-15'
WHERE NOT EXISTS (
    SELECT 1 FROM gestion_pre_electoral.ciudadanos WHERE id = 1
);

-- Insert seed registro censo (safe)
INSERT INTO gestion_pre_electoral.registros_censo (
    eleccion_id,
    ciudadano_id,
    estado,
    causal_estado,
    observacion,
    actor_ultima_modificacion
)
SELECT
    1,
    1,
    'HABILITADO',
    NULL,
    'Registro semilla',
    'seed-script'
WHERE NOT EXISTS (
    SELECT 1 FROM gestion_pre_electoral.registros_censo
    WHERE eleccion_id = 1 AND ciudadano_id = 1
);

-- Insert seed candidatura compatible with current migrations.
-- Newer migrations will normalize historic values if needed.
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
)
SELECT
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
WHERE NOT EXISTS (
    SELECT 1 FROM gestion_pre_electoral.candidaturas
    WHERE eleccion_id = 1 AND documento = '900000001'
);
