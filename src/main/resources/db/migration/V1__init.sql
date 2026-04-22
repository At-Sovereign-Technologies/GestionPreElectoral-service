CREATE SCHEMA gestion_pre_electoral;

-- =========================
-- CIUDADANOS
-- =========================
CREATE TABLE gestion_pre_electoral.ciudadanos (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    nombres VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    fecha_nacimiento DATE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ciudadano_documento UNIQUE (tipo_documento, numero_documento)
);

-- =========================
-- REGISTROS CENSO
-- =========================
CREATE TABLE gestion_pre_electoral.registros_censo (
    id BIGSERIAL PRIMARY KEY,
    eleccion_id BIGINT NOT NULL,
    ciudadano_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    causal_estado VARCHAR(50),
    observacion VARCHAR(500),
    actor_ultima_modificacion VARCHAR(120) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_registro_censo UNIQUE (eleccion_id, ciudadano_id),

    CONSTRAINT fk_registro_censo_ciudadano 
        FOREIGN KEY (ciudadano_id)
        REFERENCES gestion_pre_electoral.ciudadanos (id),

    CONSTRAINT ck_registro_censo_estado 
        CHECK (estado IN ('HABILITADO', 'EXCLUIDO', 'EXENTO')),

    CONSTRAINT ck_registro_censo_causal_estado 
        CHECK (
            causal_estado IS NULL OR causal_estado IN (
                'FUERZA_PUBLICA_ACTIVA',
                'INTERDICCION_JUDICIAL',
                'CONDENA_CON_PENA_ACCESORIA',
                'MAYOR_LIMITE_EDAD',
                'DISCAPACIDAD_REGISTRADA'
            )
        )
);

-- =========================
-- CANDIDATURAS
-- =========================
CREATE TABLE gestion_pre_electoral.candidaturas (
    id BIGSERIAL PRIMARY KEY,
    eleccion_id BIGINT NOT NULL,
    nombre_candidato VARCHAR(180) NOT NULL,
    documento VARCHAR(30) NOT NULL,
    partido VARCHAR(120) NOT NULL,
    circunscripcion VARCHAR(120) NOT NULL,
    foto_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL,
    candidatura_reemplazada_id BIGINT,
    motivo_reemplazo VARCHAR(20),
    justificacion_reemplazo VARCHAR(500),
    actor_ultima_modificacion VARCHAR(120) NOT NULL,
    fecha_inscripcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_candidatura_eleccion_documento 
        UNIQUE (eleccion_id, documento),

    CONSTRAINT fk_candidatura_reemplazada 
        FOREIGN KEY (candidatura_reemplazada_id)
        REFERENCES gestion_pre_electoral.candidaturas (id),

    CONSTRAINT ck_candidatura_estado 
        CHECK (
            estado IN (
                'INSCRITA', 
                'EN_REVISION', 
                'ACEPTADA', 
                'RECHAZADA', 
                'REVOCADA', 
                'REEMPLAZADA'
            )
        ),

    CONSTRAINT ck_candidatura_motivo_reemplazo 
        CHECK (
            motivo_reemplazo IS NULL OR 
            motivo_reemplazo IN ('MUERTE', 'INCAPACIDAD')
        )
);

-- =========================
-- ÍNDICES
-- =========================
CREATE INDEX idx_registros_censo_eleccion_id
    ON gestion_pre_electoral.registros_censo (eleccion_id);

CREATE INDEX idx_registros_censo_ciudadano_id
    ON gestion_pre_electoral.registros_censo (ciudadano_id);

CREATE INDEX idx_candidaturas_eleccion_id
    ON gestion_pre_electoral.candidaturas (eleccion_id);

CREATE INDEX idx_candidaturas_reemplazada_id
    ON gestion_pre_electoral.candidaturas (candidatura_reemplazada_id);