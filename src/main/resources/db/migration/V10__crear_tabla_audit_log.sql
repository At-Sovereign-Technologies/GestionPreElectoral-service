-- V10: Immutable audit_log table with hash chaining (WORM)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    actor_id VARCHAR(120) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    timestamp_ntp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    device_id VARCHAR(255),
    payload_hash VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    chain_hash VARCHAR(64) NOT NULL,
    CONSTRAINT uq_audit_log_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_audit_log_actor_id ON gestion_pre_electoral.audit_log (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON gestion_pre_electoral.audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON gestion_pre_electoral.audit_log (timestamp_ntp);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON gestion_pre_electoral.audit_log (action);

-- WORM trigger: block UPDATE and DELETE on audit_log
CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_bloquear_audit_log_worm() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is an immutable WORM table. UPDATE and DELETE operations are strictly prohibited.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bloquear_audit_log_worm ON gestion_pre_electoral.audit_log;
CREATE TRIGGER trg_bloquear_audit_log_worm
    BEFORE UPDATE OR DELETE ON gestion_pre_electoral.audit_log
    FOR EACH ROW EXECUTE FUNCTION gestion_pre_electoral.fn_bloquear_audit_log_worm();

-- Prevent INSERT from external connections (application must use logEvent() via service layer)
-- This is informational; the application-layer logEvent() is the authoritative gate.
-- A DB-level rule can be added if superadmin API access needs to be locked down further:
-- For now, we rely on application-layer enforcement (ServicioAuditoria has no update/delete)
