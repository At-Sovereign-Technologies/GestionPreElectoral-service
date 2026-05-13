-- V10 (SR-M3-03): Extiende lista_blanca_auditoria con campos estructurados
-- para trazar las modificaciones de emergencia con firmantes, zonas y
-- referencia al evento de SR-M6.

ALTER TABLE gestion_pre_electoral.lista_blanca_auditoria
    ADD COLUMN IF NOT EXISTS zona_anterior VARCHAR(200),
    ADD COLUMN IF NOT EXISTS zona_nueva VARCHAR(200),
    ADD COLUMN IF NOT EXISTS firmante_superadmin VARCHAR(120),
    ADD COLUMN IF NOT EXISTS firmante_cne VARCHAR(120),
    ADD COLUMN IF NOT EXISTS evento_auditoria_id BIGINT;
