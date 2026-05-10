-- V9: Crear tabla lista_blanca y auditoria + datos de prueba
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.lista_blanca (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ciudadano_id VARCHAR(100) NOT NULL,
    eleccion_id BIGINT NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    telefono_celular VARCHAR(50),
    correo_electronico VARCHAR(200),
    hash_biometrico_facial VARCHAR(256),
    zona_inscripcion VARCHAR(200),
    fecha_enrolamiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS gestion_pre_electoral.lista_blanca_auditoria (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lista_blanca_id UUID NOT NULL,
    justificacion TEXT,
    firmas_json JSONB,
    version_hash VARCHAR(256),
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE IF EXISTS gestion_pre_electoral.lista_blanca_auditoria
    ADD CONSTRAINT fk_lista_blanca_auditoria_lista_blanca
    FOREIGN KEY (lista_blanca_id) REFERENCES gestion_pre_electoral.lista_blanca (id) ON DELETE CASCADE;

-- Insertar 50 registros mock realistas
DO $$
DECLARE
    i INT;
    v_hash TEXT;
    v_email TEXT;
    v_telefono TEXT;
    v_zona TEXT;
BEGIN
    FOR i IN 1..50 LOOP
        v_hash := lpad(md5(random()::text || clock_timestamp()::text), 64, '0');
        v_email := format('usuario%s@example.com', i);
        v_telefono := format('3%09s', (100000000 + i));
        v_zona := CASE (i % 5)
            WHEN 0 THEN 'BOGOTA'
            WHEN 1 THEN 'MEDELLIN'
            WHEN 2 THEN 'CALLE 45'
            WHEN 3 THEN 'CALI'
            ELSE 'BARRANQUILLA'
        END;

        INSERT INTO gestion_pre_electoral.lista_blanca (
            ciudadano_id, eleccion_id, numero_documento, telefono_celular, correo_electronico,
            hash_biometrico_facial, zona_inscripcion, fecha_enrolamiento, estado
        ) VALUES (
            uuid_generate_v4()::text || '-' || i,
            1 + (i % 4),
            format('1%08s', 10000000 + i),
            v_telefono,
            v_email,
            v_hash,
            v_zona,
            now() - (i || ' days')::interval,
            'HABILITADO'
        );
    END LOOP;
END $$;

-- Función trigger que impide UPDATE/DELETE si la elección asociada está CERRADA
CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_proteger_lista_blanca() RETURNS trigger AS $$
BEGIN
    -- Si la tabla eleccion no existe, no hacemos nada (compatibilidad con entornos de prueba)
    IF to_regclass('gestion_pre_electoral.eleccion') IS NULL THEN
        RETURN NEW;
    END IF;

    PERFORM 1 FROM gestion_pre_electoral.eleccion e WHERE e.id = COALESCE(NEW.eleccion_id, OLD.eleccion_id) AND e.estado = 'CERRADA';
    IF FOUND THEN
        RAISE EXCEPTION 'La lista está firmada y la elección está CERRADA; modificaciones prohibidas por integridad cryptográfica';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger BEFORE UPDATE OR DELETE
DROP TRIGGER IF EXISTS trg_proteger_lista_blanca ON gestion_pre_electoral.lista_blanca;
CREATE TRIGGER trg_proteger_lista_blanca
BEFORE UPDATE OR DELETE ON gestion_pre_electoral.lista_blanca
FOR EACH ROW EXECUTE FUNCTION gestion_pre_electoral.fn_proteger_lista_blanca();
