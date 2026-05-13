-- V13: Crear tablas de boveda y ceremonia + agregar campos MFA y rol a lista_blanca (US-SR-M7-03)
-- Separacion logica de credenciales: Plane A (web session) vs Plane B (vault key shard)

-- 1. Agregar campos MFA + rol + contrasena_hash a lista_blanca
ALTER TABLE gestion_pre_electoral.lista_blanca
    ADD COLUMN IF NOT EXISTS rol VARCHAR(50),
    ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS mfa_secret VARCHAR(512),
    ADD COLUMN IF NOT EXISTS mfa_configured_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS mfa_method VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN IF NOT EXISTS failed_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_failed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS contrasena_hash VARCHAR(256);

COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.rol IS 'Rol del usuario: ADMINISTRADOR | CIUDADANO | MAGISTRADO | MIXED';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.mfa_enabled IS 'Indica si el usuario tiene MFA activo';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.mfa_secret IS 'Secreto TOTP (mock: MOCK_TOTP_SECRET) - nunca almacenar en plaintext en produccion';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.mfa_configured_at IS 'Timestamp de cuando se configuró MFA';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.mfa_method IS 'Metodo MFA: TOTP | SMS | NONE';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.failed_attempts IS 'Contador de intentos de login fallidos';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.last_failed_at IS 'Timestamp del ultimo intento fallido';
COMMENT ON COLUMN gestion_pre_electoral.lista_blanca.contrasena_hash IS 'Hash de contrasena (bcrypt) para autenticacion local';

-- Seed: actualizar usuarios existentes con contrasena mock (bcrypt de "password123")
UPDATE gestion_pre_electoral.lista_blanca
SET contrasena_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye9jT/.rGgNLGR0nV1cX7U0pW8kXj6aJGe'
WHERE contrasena_hash IS NULL;

-- 2. Tabla de registros de clave de clavero (Plane B - Vault Key Shard)
CREATE TABLE IF NOT EXISTS gestion_pre_electoral.clavero_key_record (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    magistrado_id UUID NOT NULL,
    shard_index INT NOT NULL CHECK (shard_index BETWEEN 1 AND 10),
    shard_fingerprint VARCHAR(64) NOT NULL,
    delivery_method VARCHAR(30) NOT NULL DEFAULT 'PEM_FILE',
    delivered_at TIMESTAMP,
    last_used_ceremony_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clavero_key_record_magistrado
        FOREIGN KEY (magistrado_id) REFERENCES gestion_pre_electoral.lista_blanca(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_clavero_shard_unique UNIQUE (magistrado_id, shard_index)
);

COMMENT ON TABLE gestion_pre_electoral.clavero_key_record IS
'Registros de shards de clave de boveda. El shard nunca se almacena en plaintext - solo su SHA-256 fingerprint.';
COMMENT ON COLUMN gestion_pre_electoral.clavero_key_record.shard_fingerprint IS
'SHA-256 del valor del shard. Nunca almacena el shard en plaintext.';
COMMENT ON COLUMN gestion_pre_electoral.clavero_key_record.delivery_method IS
'Metodo de entrega: HARDWARE_TOKEN | PEM_FILE | PHYSICAL';

-- 3. Tabla de sesiones de ceremonia
CREATE TABLE IF NOT EXISTS gestion_pre_electoral.ceremony_session (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ceremony_type VARCHAR(20) NOT NULL DEFAULT 'APERTURA',
    initiated_by UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    required_shards INT NOT NULL DEFAULT 3,
    submitted_shards INT NOT NULL DEFAULT 0,
    activated_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ceremony_initiated_by
        FOREIGN KEY (initiated_by) REFERENCES gestion_pre_electoral.lista_blanca(id),
    CONSTRAINT chk_ceremony_status CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'ABORTED')),
    CONSTRAINT chk_ceremony_type CHECK (ceremony_type IN ('APERTURA', 'CIERRE'))
);

COMMENT ON TABLE gestion_pre_electoral.ceremony_session IS
'Ceremonias de apertura/cierre de boveda. Requiere presentacion fisica de shards (Plane B) + sesion web activa (Plane A).';

CREATE INDEX IF NOT EXISTS idx_ceremony_session_status ON gestion_pre_electoral.ceremony_session(status);
CREATE INDEX IF NOT EXISTS idx_ceremony_session_expires ON gestion_pre_electoral.ceremony_session(expires_at);
CREATE INDEX IF NOT EXISTS idx_clavero_magistrado ON gestion_pre_electoral.clavero_key_record(magistrado_id);

-- 4. Seed: crear 3 claveros mock con shards simulados
DO $$
DECLARE
    clavero1_uuid UUID;
    clavero2_uuid UUID;
    clavero3_uuid UUID;
BEGIN
    INSERT INTO gestion_pre_electoral.lista_blanca (
        ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
        hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado, rol,
        contrasena_hash, mfa_enabled, mfa_method
    ) VALUES (
        'clavero-magistrado-001', 1, '99999001', '3109990001', 'clavero1@magistrados.gov.co',
        lpad(md5(random()::text), 64, '0'), 'BOGOTA', now(), 'HABILITADO', 'MAGISTRADO',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye9jT/.rGgNLGR0nV1cX7U0pW8kXj6aJGe', true, 'TOTP'
    ) RETURNING id INTO clavero1_uuid;

    INSERT INTO gestion_pre_electoral.lista_blanca (
        ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
        hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado, rol,
        contrasena_hash, mfa_enabled, mfa_method
    ) VALUES (
        'clavero-magistrado-002', 1, '99999002', '3109990002', 'clavero2@magistrados.gov.co',
        lpad(md5(random()::text), 64, '0'), 'BOGOTA', now(), 'HABILITADO', 'MAGISTRADO',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye9jT/.rGgNLGR0nV1cX7U0pW8kXj6aJGe', true, 'TOTP'
    ) RETURNING id INTO clavero2_uuid;

    INSERT INTO gestion_pre_electoral.lista_blanca (
        ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
        hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado, rol,
        contrasena_hash, mfa_enabled, mfa_method
    ) VALUES (
        'clavero-magistrado-003', 1, '99999003', '3109990003', 'clavero3@magistrados.gov.co',
        lpad(md5(random()::text), 64, '0'), 'BOGOTA', now(), 'HABILITADO', 'MAGISTRADO',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye9jT/.rGgNLGR0nV1cX7U0pW8kXj6aJGe', true, 'TOTP'
    ) RETURNING id INTO clavero3_uuid;

    INSERT INTO gestion_pre_electoral.clavero_key_record
        (magistrado_id, shard_index, shard_fingerprint, delivery_method, delivered_at)
    VALUES
        (clavero1_uuid, 1, encode(sha256('MOCK_SHARD_INDEX_1_VALUE'::bytea), 'hex'), 'PEM_FILE', now()),
        (clavero2_uuid, 2, encode(sha256('MOCK_SHARD_INDEX_2_VALUE'::bytea), 'hex'), 'PEM_FILE', now()),
        (clavero3_uuid, 3, encode(sha256('MOCK_SHARD_INDEX_3_VALUE'::bytea), 'hex'), 'PEM_FILE', now());
END $$;
