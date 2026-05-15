-- V14: Seed auth users with bcrypt passwords and role assignments
-- Password for all users: password123
-- BCrypt hash: $2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy
-- Maps to GestionPreElectoral RolUsuario enum values

-- Admin users (Registraduria staff) — these do NOT correspond to ciudadanos in the census
-- They are operational accounts for managing the electoral system
INSERT INTO gestion_pre_electoral.lista_blanca (
    ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
    hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado,
    rol, contrasena_hash, mfa_enabled, mfa_method
) VALUES
    ('admin-rnec-001', 1, '10000001', '3101000001', 'admin.rnec@sello-legitimo.gov.co',
     md5(random()::text), 'BOGOTA', now(), 'HABILITADO', 'ADMINISTRADOR',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', true, 'TOTP'),
    ('delegado-cne-001', 1, '10000002', '3101000002', 'delegado.cne@sello-legitimo.gov.co',
     md5(random()::text), 'BOGOTA', now(), 'HABILITADO', 'SUPERADMIN',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', true, 'TOTP'),
    ('auditor-001', 1, '10000003', '3101000003', 'auditor@sello-legitimo.gov.co',
     md5(random()::text), 'BOGOTA', now(), 'HABILITADO', 'AUDITOR',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', true, 'TOTP'),
    ('operador-001', 1, '10000005', '3101000005', 'operador.mesa@sello-legitimo.gov.co',
     md5(random()::text), 'MEDELLIN', now(), 'HABILITADO', 'OPERADOR',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', true, 'TOTP')
ON CONFLICT DO NOTHING;

-- Ciudadano users — these correspond to existing ciudadanos in the census
-- Juan Perez (id=8, CC 1078901234) and Ana Gomez (id=1, CC 1000000001) are from V2/V4 seed data
INSERT INTO gestion_pre_electoral.lista_blanca (
    ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
    hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado,
    rol, contrasena_hash, mfa_enabled, mfa_method
) VALUES
    ('8', 1, '1078901234', '3107901234', 'juan.perez@example.com',
     md5(random()::text), 'CALLE 45', now(), 'HABILITADO', 'CIUDADANO',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', false, 'NONE'),
    ('1', 1, '1000000001', '3100000001', 'ana.gomez@example.com',
     md5(random()::text), 'BOGOTA', now(), 'HABILITADO', 'CIUDADANO',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', false, 'NONE')
ON CONFLICT DO NOTHING;

-- Also add a VOTANTE role user for completeness
INSERT INTO gestion_pre_electoral.lista_blanca (
    ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
    hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado,
    rol, contrasena_hash, mfa_enabled, mfa_method
) VALUES
    ('2', 1, '1012345678', '3112345678', 'carlos.martinez@example.com',
     md5(random()::text), 'MEDELLIN', now(), 'HABILITADO', 'VOTANTE',
     '$2b$10$Gvqr2me768Gy0tjXLDcYuezNzoJsYhkxlJOS0wi0oAeEXS0rAECvy', false, 'NONE')
ON CONFLICT DO NOTHING;