-- ============================================================
-- V3.5: Add missing columns required by V4 seed data
-- ============================================================
-- V4__seed_rich_data.sql references these columns but they were
-- added in later migrations (V5 for geo columns, and hash_biometrico
-- was never added). This bridge migration ensures V4 can execute.

ALTER TABLE gestion_pre_electoral.registros_censo
    ADD COLUMN IF NOT EXISTS hash_biometrico VARCHAR(64);

ALTER TABLE gestion_pre_electoral.ciudadanos
    ADD COLUMN IF NOT EXISTS departamento VARCHAR(80);

ALTER TABLE gestion_pre_electoral.ciudadanos
    ADD COLUMN IF NOT EXISTS municipio VARCHAR(120);
