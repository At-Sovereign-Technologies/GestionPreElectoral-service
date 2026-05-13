CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.estado_congelamiento_censo (
	eleccion_id BIGINT PRIMARY KEY,
	estado_eleccion VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
	censo_congelado BOOLEAN NOT NULL DEFAULT FALSE,
	hash_raiz_censo VARCHAR(64),
	fecha_congelamiento TIMESTAMP,
	actor_congelamiento VARCHAR(120)
);

ALTER TABLE gestion_pre_electoral.registros_censo
	ADD COLUMN IF NOT EXISTS hash_biometrico VARCHAR(64);

UPDATE gestion_pre_electoral.registros_censo r
SET hash_biometrico = encode(
	digest(
		concat_ws('|',
			c.tipo_documento,
			c.numero_documento,
			c.nombres,
			c.apellidos,
			coalesce(to_char(c.fecha_nacimiento, 'YYYY-MM-DD'), ''),
			coalesce(c.departamento, ''),
			coalesce(c.municipio, '')
		),
		'sha256'
	),
	'hex'
)
FROM gestion_pre_electoral.ciudadanos c
WHERE c.id = r.ciudadano_id
  AND (r.hash_biometrico IS NULL OR r.hash_biometrico = '');

ALTER TABLE gestion_pre_electoral.registros_censo
	ALTER COLUMN hash_biometrico SET NOT NULL;

CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_bloquear_registros_censo_congelado()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
	v_eleccion_id BIGINT;
	v_estado_eleccion VARCHAR(20);
	v_censo_congelado BOOLEAN;
BEGIN
	v_eleccion_id := COALESCE(NEW.eleccion_id, OLD.eleccion_id);

	SELECT e.estado_eleccion, e.censo_congelado
	INTO v_estado_eleccion, v_censo_congelado
	FROM gestion_pre_electoral.estado_congelamiento_censo e
	WHERE e.eleccion_id = v_eleccion_id;

	IF FOUND AND (v_estado_eleccion = 'CERRADA' OR v_censo_congelado) THEN
		RAISE EXCEPTION USING
			ERRCODE = '42501',
			MESSAGE = format('El censo de la elección %s está congelado y no permite modificaciones', v_eleccion_id);
	END IF;

	RETURN COALESCE(NEW, OLD);
END;
$$;

DROP TRIGGER IF EXISTS tr_bloquear_registros_censo_congelado ON gestion_pre_electoral.registros_censo;

CREATE TRIGGER tr_bloquear_registros_censo_congelado
BEFORE INSERT OR UPDATE OR DELETE ON gestion_pre_electoral.registros_censo
FOR EACH ROW
EXECUTE FUNCTION gestion_pre_electoral.fn_bloquear_registros_censo_congelado();