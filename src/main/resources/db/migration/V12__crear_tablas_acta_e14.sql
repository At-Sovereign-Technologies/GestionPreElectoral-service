-- V11: Acta E-14 tables with full lifecycle traceability
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.acta_e14 (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mesa_id VARCHAR(100) NOT NULL,
    eleccion_id BIGINT NOT NULL,
    numero_formulario VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) NOT NULL DEFAULT 'CREADA'
);

CREATE INDEX IF NOT EXISTS idx_acta_e14_mesa ON gestion_pre_electoral.acta_e14 (mesa_id);
CREATE INDEX IF NOT EXISTS idx_acta_e14_eleccion ON gestion_pre_electoral.acta_e14 (eleccion_id);
CREATE INDEX IF NOT EXISTS idx_acta_e14_estado ON gestion_pre_electoral.acta_e14 (estado);

-- WORM trigger: block UPDATE and DELETE on acta_e14 (corrections are non-destructive via lifecycle)
CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_bloquear_acta_e14_worm() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'acta_e14 is immutable. Legal corrections must be applied via the lifecycle table (acta_e14_lifecycle). Direct UPDATE/DELETE on this table is prohibited.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bloquear_acta_e14_worm ON gestion_pre_electoral.acta_e14;
CREATE TRIGGER trg_bloquear_acta_e14_worm
    BEFORE UPDATE OR DELETE ON gestion_pre_electoral.acta_e14
    FOR EACH ROW EXECUTE FUNCTION gestion_pre_electoral.fn_bloquear_acta_e14_worm();

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.acta_e14_lifecycle (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    acta_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    previous_version_id UUID,
    estado VARCHAR(50) NOT NULL,
    timestamp_ntp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_id VARCHAR(120) NOT NULL,
    device_id VARCHAR(255),
    document_sha256 VARCHAR(64),
    authorization_ref VARCHAR(255),
    metadata JSONB,
    CONSTRAINT fk_acta_e14_lifecycle_acta
        FOREIGN KEY (acta_id) REFERENCES gestion_pre_electoral.acta_e14(id) ON DELETE CASCADE,
    CONSTRAINT fk_acta_e14_lifecycle_prev_version
        FOREIGN KEY (previous_version_id) REFERENCES gestion_pre_electoral.acta_e14_lifecycle(id),
    CONSTRAINT uq_acta_e14_lifecycle_acta_version
        UNIQUE (acta_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_acta_e14_lifecycle_acta ON gestion_pre_electoral.acta_e14_lifecycle (acta_id);
CREATE INDEX IF NOT EXISTS idx_acta_e14_lifecycle_estado ON gestion_pre_electoral.acta_e14_lifecycle (estado);
CREATE INDEX IF NOT EXISTS idx_acta_e14_lifecycle_actor ON gestion_pre_electoral.acta_e14_lifecycle (actor_id);
CREATE INDEX IF NOT EXISTS idx_acta_e14_lifecycle_timestamp ON gestion_pre_electoral.acta_e14_lifecycle (timestamp_ntp);

-- WORM trigger: block UPDATE and DELETE on lifecycle records
CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_bloquear_acta_e14_lifecycle_worm() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'acta_e14_lifecycle is an immutable WORM table. Audit records cannot be modified or deleted.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bloquear_acta_e14_lifecycle_worm ON gestion_pre_electoral.acta_e14_lifecycle;
CREATE TRIGGER trg_bloquear_acta_e14_lifecycle_worm
    BEFORE UPDATE OR DELETE ON gestion_pre_electoral.acta_e14_lifecycle
    FOR EACH ROW EXECUTE FUNCTION gestion_pre_electoral.fn_bloquear_acta_e14_lifecycle_worm();
