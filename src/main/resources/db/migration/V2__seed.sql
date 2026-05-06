DO $$
DECLARE
    v_schema_exists BOOLEAN;
BEGIN
    -- Insert seed ciudadano (safe, no constraint issues)
    INSERT INTO gestion_pre_electoral.ciudadanos (
        id,
        tipo_documento,
        numero_documento,
        nombres,
        apellidos,
        fecha_nacimiento
    ) VALUES (
        1,
        'CC',
        '1000000001',
        'Ana',
        'Gomez',
        '1990-02-15'
    ) ON CONFLICT (id) DO NOTHING;

    -- Insert seed registro censo (safe)
    INSERT INTO gestion_pre_electoral.registros_censo (
        eleccion_id,
        ciudadano_id,
        estado,
        causal_estado,
        observacion,
        actor_ultima_modificacion
    ) VALUES (
        1,
        1,
        'HABILITADO',
        NULL,
        'Registro semilla',
        'seed-script'
    ) ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;

    -- Detect whether V3 schema changes are already present (version column)
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'gestion_pre_electoral' 
          AND table_name = 'candidaturas' 
          AND column_name = 'version'
    ) INTO v_schema_exists;

    -- Insert seed candidatura with state compatible with current schema
    IF v_schema_exists THEN
        -- V3+ schema: use POSTULADO (valid in new constraint)
        INSERT INTO gestion_pre_electoral.candidaturas (
            eleccion_id,
            nombre_candidato,
            documento,
            partido,
            circunscripcion,
            foto_url,
            estado,
            candidatura_reemplazada_id,
            motivo_reemplazo,
            justificacion_reemplazo,
            actor_ultima_modificacion
        ) VALUES (
            1,
            'Luis Perez',
            '900000001',
            'Partido Demo',
            'NACIONAL',
            NULL,
            'POSTULADO',
            NULL,
            NULL,
            NULL,
            'seed-script'
        ) ON CONFLICT (eleccion_id, documento) DO NOTHING;
    ELSE
        -- V1 schema only: use INSCRITA (valid in original constraint)
        INSERT INTO gestion_pre_electoral.candidaturas (
            eleccion_id,
            nombre_candidato,
            documento,
            partido,
            circunscripcion,
            foto_url,
            estado,
            candidatura_reemplazada_id,
            motivo_reemplazo,
            justificacion_reemplazo,
            actor_ultima_modificacion
        ) VALUES (
            1,
            'Luis Perez',
            '900000001',
            'Partido Demo',
            'NACIONAL',
            NULL,
            'INSCRITA',
            NULL,
            NULL,
            NULL,
            'seed-script'
        ) ON CONFLICT (eleccion_id, documento) DO NOTHING;
    END IF;
END $$;
