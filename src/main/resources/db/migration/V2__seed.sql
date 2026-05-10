DO $$
DECLARE
    v_schema_exists BOOLEAN;
    v_hash_biometrico_exists BOOLEAN;
    v_hash_biometrico VARCHAR(64);
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

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'gestion_pre_electoral'
          AND table_name = 'registros_censo'
          AND column_name = 'hash_biometrico'
    ) INTO v_hash_biometrico_exists;

    IF v_hash_biometrico_exists THEN
        SELECT encode(
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
        INTO v_hash_biometrico
        FROM gestion_pre_electoral.ciudadanos c
        WHERE c.id = 1;
    END IF;

    -- Insert seed registro censo (safe)
    IF v_hash_biometrico_exists THEN
        INSERT INTO gestion_pre_electoral.registros_censo (
            eleccion_id,
            ciudadano_id,
            estado,
            causal_estado,
            observacion,
            actor_ultima_modificacion,
            hash_biometrico
        ) VALUES (
            1,
            1,
            'HABILITADO',
            NULL,
            'Registro semilla',
            'seed-script',
            v_hash_biometrico
        ) ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;
    ELSE
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
    END IF;

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
