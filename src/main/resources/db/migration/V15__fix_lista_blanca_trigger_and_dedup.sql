-- V15: Fix lista_blanca trigger for DELETE operations and deduplicate rows
--
-- Bug: fn_proteger_lista_blanca() returned NEW for all operations.
-- For BEFORE DELETE triggers, NEW is NULL, which silently blocks ALL deletes.
-- Fix: return OLD for DELETE (allow delete when election is not CERRADA).
-- Also add a unique constraint on numero_documento to prevent future duplicates.

CREATE OR REPLACE FUNCTION gestion_pre_electoral.fn_proteger_lista_blanca() RETURNS trigger AS $$
BEGIN
    IF to_regclass('gestion_pre_electoral.eleccion') IS NULL THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        RETURN NEW;
    END IF;

    PERFORM 1 FROM gestion_pre_electoral.eleccion e WHERE e.id = COALESCE(NEW.eleccion_id, OLD.eleccion_id) AND e.estado = 'CERRADA';
    IF FOUND THEN
        RAISE EXCEPTION 'La lista está firmada y la elección está CERRADA; modificaciones prohibidas por integridad cryptográfica';
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Deduplicate lista_blanca: keep one row per numero_documento
-- Disable trigger first to allow deletes
ALTER TABLE gestion_pre_electoral.lista_blanca DISABLE TRIGGER trg_proteger_lista_blanca;

DELETE FROM gestion_pre_electoral.lista_blanca a
USING gestion_pre_electoral.lista_blanca b
WHERE a.numero_documento = b.numero_documento
AND a.id::text > b.id::text;

-- Add unique constraint on numero_documento
CREATE UNIQUE INDEX IF NOT EXISTS idx_lista_blanca_numero_documento_unique
ON gestion_pre_electoral.lista_blanca (numero_documento);

-- Re-enable trigger
ALTER TABLE gestion_pre_electoral.lista_blanca ENABLE TRIGGER trg_proteger_lista_blanca;