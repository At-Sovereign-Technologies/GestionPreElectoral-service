-- =========================
-- ELIMINAR CHECK CONSTRAINT VIEJO (idempotente)
-- =========================
ALTER TABLE gestion_pre_electoral.candidaturas
    DROP CONSTRAINT IF EXISTS ck_candidatura_estado;

-- =========================
-- MIGRACIÓN DE ESTADOS LEGACY → NUEVOS (naturalmente idempotente)
-- =========================
UPDATE gestion_pre_electoral.candidaturas
SET estado = 'POSTULADO'
WHERE estado = 'INSCRITA';

UPDATE gestion_pre_electoral.candidaturas
SET estado = 'EN_VALIDACION'
WHERE estado = 'EN_REVISION';

UPDATE gestion_pre_electoral.candidaturas
SET estado = 'APROBADO'
WHERE estado = 'ACEPTADA';

UPDATE gestion_pre_electoral.candidaturas
SET estado = 'RECHAZADO'
WHERE estado = 'RECHAZADA';

-- =========================
-- OPTIMISTIC LOCKING (idempotente)
-- =========================
ALTER TABLE gestion_pre_electoral.candidaturas
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- =========================
-- CREAR NUEVO CHECK CONSTRAINT DE ESTADOS (idempotente)
-- =========================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_schema = 'gestion_pre_electoral' 
          AND table_name = 'candidaturas' 
          AND constraint_name = 'ck_candidatura_estado'
    ) THEN
        ALTER TABLE gestion_pre_electoral.candidaturas
            ADD CONSTRAINT ck_candidatura_estado CHECK (
                estado IN (
                    'BORRADOR',
                    'POSTULADO',
                    'EN_VALIDACION',
                    'APROBADO',
                    'RECHAZADO',
                    'PUBLICADO',
                    'BLOQUEADO',
                    'REEMPLAZADA',
                    'REVOCADA'
                )
            );
    END IF;
END $$;

-- =========================
-- TABLA DE VERSIONADO HISTÓRICO (idempotente)
-- =========================
CREATE TABLE IF NOT EXISTS gestion_pre_electoral.candidatura_versiones (
    id BIGSERIAL PRIMARY KEY,
    candidatura_id BIGINT NOT NULL,
    version_number BIGINT NOT NULL,
    nombre_candidato VARCHAR(180) NOT NULL,
    documento VARCHAR(30) NOT NULL,
    partido VARCHAR(120) NOT NULL,
    circunscripcion VARCHAR(120) NOT NULL,
    foto_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL,
    actor_modificacion VARCHAR(120) NOT NULL,
    fecha_version TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_version_candidatura
        FOREIGN KEY (candidatura_id) REFERENCES gestion_pre_electoral.candidaturas(id)
);

CREATE INDEX IF NOT EXISTS idx_versiones_candidatura_id
    ON gestion_pre_electoral.candidatura_versiones(candidatura_id);

-- =========================
-- TABLA DE EVENTOS DE AUDITORÍA LOCAL (idempotente)
-- =========================
CREATE TABLE IF NOT EXISTS gestion_pre_electoral.auditoria_eventos (
    id BIGSERIAL PRIMARY KEY,
    aggregado_tipo VARCHAR(50) NOT NULL,
    aggregado_id BIGINT NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    payload_json TEXT,
    hash_integridad VARCHAR(64) NOT NULL,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auditoria_aggregado
    ON gestion_pre_electoral.auditoria_eventos(aggregado_tipo, aggregado_id);
