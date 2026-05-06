-- ============================================================
-- V5: Add geographic columns to ciudadanos for sorteo integration
-- ============================================================
-- These columns enable censo-based jurado selection by location.
-- Both are nullable for backward compatibility with existing data
-- and CSV uploads that don't include geographic information.

ALTER TABLE gestion_pre_electoral.ciudadanos
    ADD COLUMN IF NOT EXISTS departamento VARCHAR(80);

ALTER TABLE gestion_pre_electoral.ciudadanos
    ADD COLUMN IF NOT EXISTS municipio VARCHAR(120);

-- Index for sorteo queries filtering by location
CREATE INDEX IF NOT EXISTS idx_ciudadanos_departamento
    ON gestion_pre_electoral.ciudadanos (departamento);

CREATE INDEX IF NOT EXISTS idx_ciudadanos_municipio
    ON gestion_pre_electoral.ciudadanos (municipio);

-- Backfill existing seed citizens with realistic locations
-- so they can participate in geo-scoped sorteos
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Cundinamarca', municipio = 'Bogota' WHERE id IN (1, 3, 7, 10, 13, 17, 19);
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Antioquia', municipio = 'Medellin' WHERE id IN (2, 9, 12, 16);
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Cundinamarca', municipio = 'Soacha' WHERE id IN (6, 14, 18);
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Valle del Cauca', municipio = 'Cali' WHERE id IN (4, 11, 20);
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Atlantico', municipio = 'Barranquilla' WHERE id IN (5, 8, 15);
UPDATE gestion_pre_electoral.ciudadanos SET departamento = 'Santander', municipio = 'Bucaramanga' WHERE id IN (21, 22, 23, 24);