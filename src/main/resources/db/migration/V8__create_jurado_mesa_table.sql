CREATE TABLE IF NOT EXISTS gestion_pre_electoral.jurados_mesa (
	id BIGSERIAL PRIMARY KEY,
	nombre VARCHAR(180) NOT NULL,
	documento VARCHAR(30) NOT NULL,
	mesa_id BIGINT NOT NULL,
	rol VARCHAR(50) NOT NULL,
	token_acceso VARCHAR(36) NOT NULL,

	CONSTRAINT uk_jurado_mesa_documento_mesa UNIQUE (mesa_id, documento),
	CONSTRAINT uk_jurado_mesa_token UNIQUE (token_acceso)
);

CREATE INDEX IF NOT EXISTS idx_jurados_mesa_mesa_id
	ON gestion_pre_electoral.jurados_mesa (mesa_id);

CREATE INDEX IF NOT EXISTS idx_jurados_mesa_token_acceso
	ON gestion_pre_electoral.jurados_mesa (token_acceso);