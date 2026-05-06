-- ============================================================
-- V6: Massive seed data — 100K+ ciudadanos and censo records
-- ============================================================
-- Generates realistic Colombian demographic data using PL/pgSQL.
-- Uses ON CONFLICT DO NOTHING for idempotency.
-- Resets sequences after manual inserts.
-- ============================================================

DO $$
DECLARE
    v_nombres TEXT[] := ARRAY[
        'Juan','Carlos','Jose','Luis','Andres','Diego','Felipe','Santiago','Camilo','Esteban',
        'Pedro','Ricardo','Oscar','Fernando','Hector','Manuel','Alejandro','Sebastian','Daniel','Nicolas',
        'Maria','Carmen','Ana','Laura','Paula','Diana','Valentina','Daniela','Alejandra','Manuela',
        'Natalia','Monica','Rosa','Adriana','Patricia','Lucia','Gabriela','Mariana','Juliana','Carolina',
        'Catalina','Isabella','Sofia','Marta','Elena','Claudia','Liliana','Gloria','Betty','Yolanda'
    ];
    v_apellidos TEXT[] := ARRAY[
        'Garcia','Rodriguez','Martinez','Lopez','Gonzalez','Perez','Sanchez','Ramirez','Torres','Flores',
        'Rivera','Gomez','Diaz','Moreno','Jimenez','Mendoza','Ortiz','Cardenas','Rojas','Castillo',
        'Vargas','Reyes','Hernandez','Alvarez','Romero','Contreras','Medina','Silva','Pardo','Arias',
        ' Munoz','Castro','Delgado','Guerrero','Navarro','Rios','Morales','Salazar','Luna','Mejia',
        'Ospina','Restrepo','Carvajal','Duque','Montoya','Trujillo','Benavides','Cabrera','Herrera','Sanabria'
    ];
    v_tipos_documento TEXT[] := ARRAY['CC','CC','CC','CC','CC','CC','CC','CC','CC','TI'];
    v_departamentos TEXT[] := ARRAY[
        'Amazonas','Antioquia','Arauca','Atlantico','Bolivar','Boyaca','Caldas','Caqueta','Casanare','Cauca',
        'Cesar','Choco','Cordoba','Cundinamarca','Guainia','Guaviare','Huila','La Guajira','Magdalena','Meta',
        'Narino','Norte de Santander','Putumayo','Quindio','Risaralda','San Andres','Santander','Sucre','Tolima','Valle del Cauca',
        'Vaupes','Vichada','Bogota'
    ];
    -- Weight distribution: Bogota has ~20%, Antioquia ~12%, Valle ~8%, Atlantico ~7%, rest distributed
    v_departamento_pesos NUMERIC[] := ARRAY[
        1,12,1,7,4,4,3,2,1,2,
        3,1,4,8,0.5,0.5,3,2,3,3,
        4,3,1,2,3,0.5,5,3,3,8,
        0.5,0.5,20
    ];
    -- Helper function to get municipios for a department (avoids jagged 2D array)
    v_mun_array TEXT[];
    v_estado_censo TEXT[] := ARRAY['HABILITADO','HABILITADO','HABILITADO','HABILITADO','HABILITADO',
                                     'HABILITADO','HABILITADO','EXCLUIDO','EXENTO','HABILITADO'];
    v_causales_excluido TEXT[] := ARRAY['INTERDICCION_JUDICIAL','CONDENA_CON_PENA_ACCESORIA'];
    v_causales_exento TEXT[] := ARRAY['FUERZA_PUBLICA_ACTIVA','MAYOR_LIMITE_EDAD','DISCAPACIDAD_REGISTRADA'];

    v_total_ciudadanos INT := 100000;
    v_total_censo INT := 0;
    v_cedula TEXT;
    v_nombre TEXT;
    v_apellido1 TEXT;
    v_apellido2 TEXT;
    v_tipo_doc TEXT;
    v_dept_idx INT;
    v_mun_idx INT;
    v_depto TEXT;
    v_munici TEXT;
    v_anio INT;
    v_mes INT;
    v_dia INT;
    v_fecha DATE;
    v_estado TEXT;
    v_causal TEXT;
    v_observacion TEXT;
    v_ciudadano_id BIGINT;
    v_inicio_id BIGINT;
    v_rand_val FLOAT;
    v_peso_acum NUMERIC;
    v_observaciones TEXT[] := ARRAY[
        'Ciudadano habilitado','Registro actualizado','Verificacion completada','Dato verificado',
        'Importacion masiva','Registro de origen externo','Actualizacion por administrador','Registro automatico'
    ];
    v_observaciones_excl TEXT[] := ARRAY[
        'Interdiccion por incapacidad mental','Condena por delito grave con pena accesoria',
        'Sentencia judicial vigente','Pena accesoria de inhabilitacion para ejercer cargos publicos'
    ];
    v_observaciones_ext TEXT[] := ARRAY[
        'Mayor de 60 anos - exencion por edad','Discapacidad fisica certificada',
        'Personal activo fuerzas militares y policia','Adulto mayor - exencion por edad avanzada',
        'Miembro activo de la fuerza publica','Discapacidad cognitiva registrada'
    ];
    v_eleccion_ids BIGINT[] := ARRAY[1,2,3,4];
    v_eleccion_idx INT;
    v_registro_count INT;
    v_batch_size INT := 5000;
    v_max_id BIGINT;
BEGIN
    -- ============================================================
    -- 1. GENERATE CIUDADANOS
    -- ============================================================
    v_inicio_id := COALESCE((SELECT MAX(id) FROM gestion_pre_electoral.ciudadanos), 0) + 1;

    FOR i IN 1..v_total_ciudadanos LOOP
        -- Generate cedula: numeric range 5M to 95M
        v_cedula := TO_CHAR(v_inicio_id + i - 1 + 10000000, 'FM9999999999');

        -- Random names using PL/pgSQL RANDOM
        v_nombre := v_nombres[FLOOR(RANDOM() * ARRAY_LENGTH(v_nombres, 1))::INT + 1];
        v_apellido1 := v_apellidos[FLOOR(RANDOM() * ARRAY_LENGTH(v_apellidos, 1))::INT + 1];
        v_apellido2 := v_apellidos[FLOOR(RANDOM() * ARRAY_LENGTH(v_apellidos, 1))::INT + 1];

        -- Document type: 90% CC, 10% TI
        v_tipo_doc := v_tipos_documento[FLOOR(RANDOM() * 10)::INT + 1];

        -- Weighted department selection
        v_rand_val := RANDOM();
        v_peso_acum := 0;
        v_dept_idx := 1;
        FOR d IN 1..ARRAY_LENGTH(v_departamento_pesos, 1) LOOP
            v_peso_acum := v_peso_acum + v_departamento_pesos[d] / 100.0;
            IF v_rand_val <= v_peso_acum THEN
                v_dept_idx := d;
                EXIT;
            END IF;
        END LOOP;
        v_depto := v_departamentos[v_dept_idx];

        -- Municipio based on department (Bogota special handling)
        IF v_depto = 'Bogota' THEN
            v_munici := 'Bogota';
        ELSE
            v_mun_array := CASE v_depto
                WHEN 'Amazonas' THEN ARRAY['Leticia']
                WHEN 'Antioquia' THEN ARRAY['Medellin','Envigado','Itagui','Bello','Rionegro','Apartado','Turbo','Caucasia']
                WHEN 'Arauca' THEN ARRAY['Arauca']
                WHEN 'Atlantico' THEN ARRAY['Barranquilla','Soledad','Malambo','Puerto Colombia','Sabanagrande']
                WHEN 'Bolivar' THEN ARRAY['Cartagena','Magangue','Turbaco']
                WHEN 'Boyaca' THEN ARRAY['Tunja','Duitama','Sogamoso','Chiquinquira']
                WHEN 'Caldas' THEN ARRAY['Manizales','La Dorada','Villamaria']
                WHEN 'Caqueta' THEN ARRAY['Florencia','San Vicente del Caguán']
                WHEN 'Casanare' THEN ARRAY['Yopal','Aguazul']
                WHEN 'Cauca' THEN ARRAY['Popayan','Santander de Quilichao']
                WHEN 'Cesar' THEN ARRAY['Valledupar','Aguachica']
                WHEN 'Choco' THEN ARRAY['Quibdo']
                WHEN 'Cordoba' THEN ARRAY['Monteria','Cerete','Lorica','Sahagun']
                WHEN 'Cundinamarca' THEN ARRAY['Bogota','Soacha','Chia','Zipaquira','Facatativa','Fusagasuga','Mosquera','Fomeque']
                WHEN 'Guainia' THEN ARRAY['Inirida']
                WHEN 'Guaviare' THEN ARRAY['San Jose del Guaviare']
                WHEN 'Huila' THEN ARRAY['Neiva','Pitalito','Garzon']
                WHEN 'La Guajira' THEN ARRAY['Riohacha','Maicao']
                WHEN 'Magdalena' THEN ARRAY['Santa Marta','Cienaga','Fundacion']
                WHEN 'Meta' THEN ARRAY['Villavicencio','Acacias','Granada']
                WHEN 'Narino' THEN ARRAY['Pasto','Tumaco','Ipiales']
                WHEN 'Norte de Santander' THEN ARRAY['Cucuta','Ocana','Pamplona']
                WHEN 'Putumayo' THEN ARRAY['Mocoa','Puerto Asis']
                WHEN 'Quindio' THEN ARRAY['Armenia','Calarca']
                WHEN 'Risaralda' THEN ARRAY['Pereira','Dosquebradas','Santa Rosa de Cabal']
                WHEN 'San Andres' THEN ARRAY['San Andres']
                WHEN 'Santander' THEN ARRAY['Bucaramanga','Floridablanca','Giron','Piedecuesta','San Gil','Barrancabermeja']
                WHEN 'Sucre' THEN ARRAY['Sincelejo','Corozal','San Marcos']
                WHEN 'Tolima' THEN ARRAY['Ibague','Espinal','Melgar','Honda']
                WHEN 'Valle del Cauca' THEN ARRAY['Cali','Buenaventura','Palmira','Tulua','Yumbo','Cartago','Buga']
                WHEN 'Vaupes' THEN ARRAY['Mitica']
                WHEN 'Vichada' THEN ARRAY['Puerto Carreno']
                ELSE ARRAY[v_depto]
            END;
            v_munici := v_mun_array[FLOOR(RANDOM() * ARRAY_LENGTH(v_mun_array, 1))::INT + 1];
        END IF;

        -- Birth date: 1945-2007
        v_anio := 1945 + FLOOR(RANDOM() * 63)::INT;
        v_mes := 1 + FLOOR(RANDOM() * 12)::INT;
        v_dia := 1 + FLOOR(RANDOM() * 28)::INT;
        v_fecha := MAKE_DATE(v_anio, v_mes, v_dia);

        INSERT INTO gestion_pre_electoral.ciudadanos
            (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, departamento, municipio)
        VALUES
            (v_tipo_doc, v_cedula, v_nombre, v_apellido1 || ' ' || v_apellido2, v_fecha, v_depto, v_munici);
    END LOOP;

    -- Reset sequence
    SELECT MAX(id) INTO v_max_id FROM gestion_pre_electoral.ciudadanos;
    PERFORM setval('gestion_pre_electoral.ciudadanos_id_seq', v_max_id, true);

    RAISE NOTICE 'Inserted % ciudadanos (IDs % to %)', v_total_ciudadanos, v_inicio_id, v_inicio_id + v_total_ciudadanos - 1;

    -- ============================================================
    -- 2. GENERATE CENSO RECORDS — elections 1-4
    -- ============================================================
    -- For efficiency, assign each citizen to 1-3 elections randomly
    -- with realistic distribution: ~70% HABILITADO, ~15% EXCLUIDO, ~15% EXENTO

    FOR cid IN v_inicio_id..v_inicio_id + v_total_ciudadanos - 1 LOOP
        -- Each citizen is enrolled in 1-3 elections
        v_registro_count := 1 + FLOOR(RANDOM() * 3)::INT;

        FOR j IN 1..v_registro_count LOOP
            v_eleccion_idx := FLOOR(RANDOM() * 4)::INT + 1;
            v_eleccion_idx := v_eleccion_ids[v_eleccion_idx];

            -- Skip if already exists (rare collision)
            IF EXISTS (SELECT 1 FROM gestion_pre_electoral.registros_censo WHERE eleccion_id = v_eleccion_idx AND ciudadano_id = cid) THEN
                CONTINUE;
            END IF;

            -- Determine estado with weighted distribution
            v_rand_val := RANDOM();
            IF v_rand_val < 0.70 THEN
                v_estado := 'HABILITADO';
                v_causal := NULL;
                v_observacion := v_observaciones[FLOOR(RANDOM() * ARRAY_LENGTH(v_observaciones, 1))::INT + 1];
            ELSIF v_rand_val < 0.85 THEN
                v_estado := 'EXCLUIDO';
                v_causal := v_causales_excluido[FLOOR(RANDOM() * ARRAY_LENGTH(v_causales_excluido, 1))::INT + 1];
                v_observacion := v_observaciones_excl[FLOOR(RANDOM() * ARRAY_LENGTH(v_observaciones_excl, 1))::INT + 1];
            ELSE
                v_estado := 'EXENTO';
                v_causal := v_causales_exento[FLOOR(RANDOM() * ARRAY_LENGTH(v_causales_exento, 1))::INT + 1];
                v_observacion := v_observaciones_ext[FLOOR(RANDOM() * ARRAY_LENGTH(v_observaciones_ext, 1))::INT + 1];
            END IF;

            INSERT INTO gestion_pre_electoral.registros_censo
                (eleccion_id, ciudadano_id, estado, causal_estado, observacion, actor_ultima_modificacion)
            VALUES
                (v_eleccion_idx, cid, v_estado, v_causal, v_observacion, 'seed-v6')
            ON CONFLICT (eleccion_id, ciudadano_id) DO NOTHING;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Censo records generated for % citizens across elections 1-4', v_total_ciudadanos;

    -- ============================================================
    -- 3. ADD ADDITIONAL CANDIDATURAS referencing existing HABILITADO citizens
    -- ============================================================
    -- Add 3 candidaturas for election 1 from newly seeded citizens
    INSERT INTO gestion_pre_electoral.candidaturas
        (eleccion_id, nombre_candidato, documento, partido, circunscripcion, foto_url, estado,
         candidatura_reemplazada_id, motivo_reemplazo, justificacion_reemplazo,
         actor_ultima_modificacion, version)
    VALUES
        (1, COALESCE((SELECT nombres || ' ' || apellidos FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id LIMIT 1), 'Candidato Seed 1'),
         (SELECT numero_documento FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id LIMIT 1), 'Partido Alianza Verde', 'NACIONAL', NULL, 'BORRADOR', NULL, NULL, NULL, 'seed-v6', 0),
        (2, COALESCE((SELECT nombres || ' ' || apellidos FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id + 1 LIMIT 1), 'Candidato Seed 2'),
         (SELECT numero_documento FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id + 1 LIMIT 1), 'Pacto Historico', 'NACIONAL', NULL, 'POSTULADO', NULL, NULL, NULL, 'seed-v6', 0),
        (3, COALESCE((SELECT nombres || ' ' || apellidos FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id + 2 LIMIT 1), 'Candidato Seed 3'),
         (SELECT numero_documento FROM gestion_pre_electoral.ciudadanos WHERE id = v_inicio_id + 2 LIMIT 1), 'Nuevo Liberalismo', 'TERRITORIAL', NULL, 'EN_VALIDACION', NULL, NULL, NULL, 'seed-v6', 0)
    ON CONFLICT (eleccion_id, documento) DO NOTHING;

    RAISE NOTICE 'Additional candidaturas added';

END $$;