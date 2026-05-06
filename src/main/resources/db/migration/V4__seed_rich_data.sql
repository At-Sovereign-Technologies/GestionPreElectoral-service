-- ============================================================
-- V4: Seed enriquecido de ciudadanos, censo y candidaturas
-- ============================================================
-- Objetivo: proveer datos de prueba consistentes entre servicios
-- para validar la UI de Gestión de Censo y Candidaturas.
--
-- Reglas de consistencia aplicadas:
--   - Todos los documentos de candidatura corresponden a ciudadanos
--     existentes en la tabla `ciudadanos`.
--   - Cada candidatura referencia un ciudadano HABILITADO en el censo
--     de la misma elección (garantiza que las reglas de negocio
--     INH-001..INH-004 puedan evaluarse sin fallar por datos faltantes).
--   - Se incluyen estados EXCLUIDO y EXENTO con causales reales para
--     que el dashboard de censo muestre distribución realista.
--   - Se resetea la secuencia de ciudadanos tras las inserciones
--     manuales para evitar colisiones con futuros INSERT sin id.
-- ============================================================

-- ------------------------------------------------------------
-- 1. CIUDADANOS  (id 1 ya existe: Ana Gomez CC 1000000001)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.ciudadanos
    (id, tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento)
VALUES
    ( 2, 'CC', '1012345678', 'Carlos',   'Martinez',  '1985-03-20'),
    ( 3, 'CC', '1023456789', 'Maria',    'Rodriguez', '1992-07-10'),
    ( 4, 'CC', '1034567890', 'Jose',     'Lopez',     '1988-11-05'),
    ( 5, 'CC', '1045678901', 'Luisa',    'Garcia',    '1995-01-30'),
    ( 6, 'CC', '1056789012', 'Pedro',    'Sanchez',   '1980-06-15'),
    ( 7, 'CC', '1067890123', 'Carmen',   'Diaz',      '1998-09-22'),
    ( 8, 'CC', '1078901234', 'Juan',     'Perez',     '1975-12-01'),
    ( 9, 'CC', '1089012345', 'Marta',    'Jimenez',   '1990-04-18'),
    (10, 'CC', '1090123456', 'Diego',    'Torres',    '2000-08-25'),
    (11, 'CC', '1101234567', 'Laura',    'Flores',    '1982-02-14'),
    (12, 'CC', '1112345678', 'Andres',   'Gomez',     '1993-05-30'),
    (13, 'CC', '1123456789', 'Paula',    'Moreno',    '1987-10-08'),
    (14, 'CC', '1134567890', 'Felipe',   'Rojas',     '2002-01-12'),
    (15, 'CC', '1145678901', 'Diana',    'Castillo',  '1970-07-04'),
    (16, 'CC', '1156789012', 'Santiago', 'Vargas',    '1999-11-19'),
    (17, 'CC', '1167890123', 'Valentina','Reyes',     '1989-03-27'),
    (18, 'CC', '1178901234', 'Camilo',   'Medina',    '1978-09-15'),
    (19, 'CC', '1189012345', 'Daniela',  'Silva',     '2005-06-08'),
    (20, 'CC', '1190123456', 'Esteban',  'Pardo',     '1965-12-30'),
    (21, 'CC', '1201234567', 'Rosa',     'Hernandez', '1940-01-01'),
    (22, 'TI', '1122334455', 'Julian',   'Arias',     '2010-03-15'),
    (23, 'CE', '987654321',  'Elena',    'Contreras', '1991-08-20'),
    (24, 'PA', 'A123456',    'Roberto',  'Medina',    '1983-11-11')
ON CONFLICT (id) DO NOTHING;

-- Reset de secuencia para que los próximos INSERT sin id no colisionen
SELECT setval('gestion_pre_electoral.ciudadanos_id_seq',
    COALESCE((SELECT MAX(id) FROM gestion_pre_electoral.ciudadanos), 1), true);

-- ------------------------------------------------------------
-- 2. REGISTROS DE CENSO  (eleccion_id = 1  – Presidencial 2026)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.registros_censo
    (eleccion_id, ciudadano_id, estado, causal_estado, observacion, actor_ultima_modificacion)
VALUES
    -- HABILITADOS
    (1,  1, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1,  2, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1,  3, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1,  6, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1,  7, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1,  9, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 10, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 12, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 13, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 14, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 16, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 17, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 18, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 19, 'HABILITADO', NULL,                          'Ciudadano habilitado', 'seed-v4'),
    (1, 23, 'HABILITADO', NULL,                          'Residente extranjero habilitado', 'seed-v4'),
    (1, 24, 'HABILITADO', NULL,                          'Ciudadano con pasaporte habilitado', 'seed-v4'),
    -- EXCLUIDOS
    (1,  4, 'EXCLUIDO',   'INTERDICCION_JUDICIAL',       'Interdicción por incapacidad mental', 'seed-v4'),
    (1,  8, 'EXCLUIDO',   'CONDENA_CON_PENA_ACCESORIA',  'Condena por delito grave con pena accesoria', 'seed-v4'),
    -- EXENTOS
    (1,  5, 'EXENTO',     'MAYOR_LIMITE_EDAD',           'Mayor de 60 años – exención por edad', 'seed-v4'),
    (1, 11, 'EXENTO',     'DISCAPACIDAD_REGISTRADA',     'Discapacidad física certificada', 'seed-v4'),
    (1, 15, 'EXENTO',     'MAYOR_LIMITE_EDAD',           'Mayor de 60 años – exención por edad', 'seed-v4'),
    (1, 20, 'EXENTO',     'FUERZA_PUBLICA_ACTIVA',       'Militar en servicio activo', 'seed-v4'),
    (1, 21, 'EXENTO',     'MAYOR_LIMITE_EDAD',           'Adulto mayor – exención por edad avanzada', 'seed-v4'),
    (1, 22, 'EXENTO',     'FUERZA_PUBLICA_ACTIVA',       'Menor de edad – miembro de fuerza pública juvenil', 'seed-v4')
ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

-- ------------------------------------------------------------
-- 3. REGISTROS DE CENSO  (eleccion_id = 2  – Legislativa 2026)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.registros_censo
    (eleccion_id, ciudadano_id, estado, causal_estado, observacion, actor_ultima_modificacion)
VALUES
    (2,  1, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2,  2, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2,  3, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2,  6, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2,  7, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2,  9, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 10, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 12, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 13, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 14, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 16, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 17, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 18, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 19, 'HABILITADO', NULL,                         'Habilitado para legislativa', 'seed-v4'),
    (2, 23, 'HABILITADO', NULL,                         'Residente extranjero habilitado', 'seed-v4'),
    (2, 24, 'HABILITADO', NULL,                         'Ciudadano con pasaporte habilitado', 'seed-v4'),
    (2,  4, 'EXCLUIDO',   'INTERDICCION_JUDICIAL',      'Interdicción judicial vigente', 'seed-v4'),
    (2,  8, 'EXCLUIDO',   'CONDENA_CON_PENA_ACCESORIA', 'Condena con pena accesoria vigente', 'seed-v4'),
    (2,  5, 'EXENTO',     'MAYOR_LIMITE_EDAD',          'Exención por edad avanzada', 'seed-v4'),
    (2, 11, 'EXENTO',     'DISCAPACIDAD_REGISTRADA',    'Exención por discapacidad', 'seed-v4'),
    (2, 15, 'EXENTO',     'MAYOR_LIMITE_EDAD',          'Exención por edad avanzada', 'seed-v4'),
    (2, 20, 'EXENTO',     'FUERZA_PUBLICA_ACTIVA',      'Exención fuerza pública activa', 'seed-v4'),
    (2, 21, 'EXENTO',     'MAYOR_LIMITE_EDAD',          'Exención adulto mayor', 'seed-v4'),
    (2, 22, 'EXENTO',     'FUERZA_PUBLICA_ACTIVA',      'Menor en fuerza pública juvenil', 'seed-v4')
ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

-- ------------------------------------------------------------
-- 4. REGISTROS DE CENSO  (eleccion_id = 3  – Gobernador Antioquia 2027)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.registros_censo
    (eleccion_id, ciudadano_id, estado, causal_estado, observacion, actor_ultima_modificacion)
VALUES
    (3, 1, 'HABILITADO', NULL, 'Habilitado gobernación Antioquia', 'seed-v4'),
    (3, 2, 'HABILITADO', NULL, 'Habilitado gobernación Antioquia', 'seed-v4'),
    (3, 6, 'HABILITADO', NULL, 'Habilitado gobernación Antioquia', 'seed-v4'),
    (3, 9, 'HABILITADO', NULL, 'Habilitado gobernación Antioquia', 'seed-v4'),
    (3, 12, 'HABILITADO', NULL, 'Habilitado gobernación Antioquia', 'seed-v4')
ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

-- ------------------------------------------------------------
-- 5. REGISTROS DE CENSO  (eleccion_id = 4  – Alcalde Bogotá 2027)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.registros_censo
    (eleccion_id, ciudadano_id, estado, causal_estado, observacion, actor_ultima_modificacion)
VALUES
    (4, 1, 'HABILITADO', NULL, 'Habilitado alcaldía Bogotá', 'seed-v4'),
    (4, 3, 'HABILITADO', NULL, 'Habilitado alcaldía Bogotá', 'seed-v4'),
    (4, 7, 'HABILITADO', NULL, 'Habilitado alcaldía Bogotá', 'seed-v4'),
    (4, 10, 'HABILITADO', NULL, 'Habilitado alcaldía Bogotá', 'seed-v4'),
    (4, 13, 'HABILITADO', NULL, 'Habilitado alcaldía Bogotá', 'seed-v4')
ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

-- ------------------------------------------------------------
-- 6. CANDIDATURAS  (eleccion_id = 1  – Presidencial 2026)
--    Solo ciudadanos HABILITADOS en el censo de la elección 1.
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.candidaturas
    (eleccion_id, nombre_candidato, documento, partido, circunscripcion, foto_url, estado,
     candidatura_reemplazada_id, motivo_reemplazo, justificacion_reemplazo,
     actor_ultima_modificacion, version)
VALUES
    (1, 'Carlos Martinez',   '1012345678', 'Partido Liberal',           'NACIONAL', NULL, 'POSTULADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Maria Rodriguez',   '1023456789', 'Partido Conservador',       'NACIONAL', NULL, 'POSTULADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Pedro Sanchez',     '1056789012', 'Partido Cambio Radical',    'NACIONAL', NULL, 'BORRADOR',        NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Carmen Diaz',       '1067890123', 'Partido de la U',           'NACIONAL', NULL, 'RECHAZADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Marta Jimenez',     '1089012345', 'Partido Centro Democratico','NACIONAL', NULL, 'BLOQUEADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Diego Torres',      '1090123456', 'Partido Verde',             'NACIONAL', NULL, 'EN_VALIDACION',   NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Andres Gomez',      '1112345678', 'Partido Liberal',           'NACIONAL', NULL, 'APROBADO',        NULL, NULL, NULL, 'seed-v4', 0),
    (1, 'Paula Moreno',      '1123456789', 'Movimiento Independiente',  'NACIONAL', NULL, 'PUBLICADO',       NULL, NULL, NULL, 'seed-v4', 0)
ON CONFLICT (eleccion_id, documento) DO NOTHING;

-- ------------------------------------------------------------
-- 7. CANDIDATURAS  (eleccion_id = 2  – Legislativa 2026)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.candidaturas
    (eleccion_id, nombre_candidato, documento, partido, circunscripcion, foto_url, estado,
     candidatura_reemplazada_id, motivo_reemplazo, justificacion_reemplazo,
     actor_ultima_modificacion, version)
VALUES
    (2, 'Felipe Rojas',      '1134567890', 'Partido Cambio Radical',    'NACIONAL', NULL, 'POSTULADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (2, 'Santiago Vargas',   '1156789012', 'Partido Liberal',           'NACIONAL', NULL, 'EN_VALIDACION',   NULL, NULL, NULL, 'seed-v4', 0),
    (2, 'Valentina Reyes',   '1167890123', 'Partido Conservador',       'NACIONAL', NULL, 'APROBADO',        NULL, NULL, NULL, 'seed-v4', 0),
    (2, 'Camilo Medina',     '1178901234', 'Partido Verde',             'NACIONAL', NULL, 'BORRADOR',        NULL, NULL, NULL, 'seed-v4', 0),
    (2, 'Daniela Silva',     '1189012345', 'Movimiento Independiente',  'NACIONAL', NULL, 'RECHAZADO',       NULL, NULL, NULL, 'seed-v4', 0)
ON CONFLICT (eleccion_id, documento) DO NOTHING;

-- ------------------------------------------------------------
-- 8. CANDIDATURAS  (eleccion_id = 3  – Gobernador Antioquia 2027)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.candidaturas
    (eleccion_id, nombre_candidato, documento, partido, circunscripcion, foto_url, estado,
     candidatura_reemplazada_id, motivo_reemplazo, justificacion_reemplazo,
     actor_ultima_modificacion, version)
VALUES
    (3, 'Ana Gomez',         '1000000001', 'Partido Liberal',           'TERRITORIAL', NULL, 'POSTULADO',    NULL, NULL, NULL, 'seed-v4', 0),
    (3, 'Carlos Martinez',   '1012345678', 'Partido Conservador',       'TERRITORIAL', NULL, 'BORRADOR',     NULL, NULL, NULL, 'seed-v4', 0)
ON CONFLICT (eleccion_id, documento) DO NOTHING;

-- ------------------------------------------------------------
-- 9. CANDIDATURAS  (eleccion_id = 4  – Alcalde Bogotá 2027)
-- ------------------------------------------------------------
INSERT INTO gestion_pre_electoral.candidaturas
    (eleccion_id, nombre_candidato, documento, partido, circunscripcion, foto_url, estado,
     candidatura_reemplazada_id, motivo_reemplazo, justificacion_reemplazo,
     actor_ultima_modificacion, version)
VALUES
    (4, 'Maria Rodriguez',   '1023456789', 'Partido Verde',             'ESPECIAL', NULL, 'POSTULADO',       NULL, NULL, NULL, 'seed-v4', 0),
    (4, 'Carmen Diaz',       '1067890123', 'Movimiento Independiente',  'ESPECIAL', NULL, 'BORRADOR',        NULL, NULL, NULL, 'seed-v4', 0)
ON CONFLICT (eleccion_id, documento) DO NOTHING;
