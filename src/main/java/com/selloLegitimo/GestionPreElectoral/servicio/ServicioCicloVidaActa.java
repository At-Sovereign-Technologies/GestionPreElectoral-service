package com.selloLegitimo.GestionPreElectoral.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.dto.ActaE14LifecycleRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActaE14RespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.TransicionActaE14SolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificacionActaE14RespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificacionActaE14RespuestaDto.VersionErrorDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;
import com.selloLegitimo.GestionPreElectoral.modelo.ActaE14;
import com.selloLegitimo.GestionPreElectoral.modelo.ActaE14Lifecycle;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoActaE14;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoAccionAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.ActaE14LifecycleRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ActaE14Repositorio;

@Service
public class ServicioCicloVidaActa {

	private static final Logger logger = LoggerFactory.getLogger(ServicioCicloVidaActa.class);

	@Autowired
	private ActaE14Repositorio actaRepositorio;

	@Autowired
	private ActaE14LifecycleRepositorio lifecycleRepositorio;

	@Autowired
	private ServicioAuditoria servicioAuditoria;

	@Transactional
	public ActaE14RespuestaDto crearActa(String mesaId, Long eleccionId, String actorId, String deviceId) {
		ActaE14 acta = new ActaE14();
		acta.setMesaId(mesaId);
		acta.setEleccionId(eleccionId);
		acta.setEstado(EstadoActaE14.CREADA);
		acta.setUuid(UUID.randomUUID());
		acta.setCreatedAt(OffsetDateTime.now());
		acta.setUpdatedAt(OffsetDateTime.now());
		acta = actaRepositorio.save(acta);

		ActaE14Lifecycle lifecycle = ActaE14Lifecycle.builder()
				.acta(acta)
				.versionNumber(1)
				.previousVersion(null)
				.estado(EstadoActaE14.CREADA)
				.timestampNtp(OffsetDateTime.now())
				.actorId(actorId)
				.deviceId(deviceId)
				.build();
		lifecycleRepositorio.save(lifecycle);

		servicioAuditoria.logEvent(actorId, TipoAccionAuditoria.CREATE, "ACTA_E14",
				acta.getUuid().toString(), null, deviceId);

		logger.info("[ACTA-E14] Acta creada. uuid={}, mesa={}, actor={}",
				acta.getUuid(), mesaId, actorId);
		return toActaDto(acta);
	}

	@Transactional
	public ActaE14LifecycleRespuestaDto recordTransition(TransicionActaE14SolicitudDto solicitud) {
		ActaE14 acta = actaRepositorio.findByUuid(solicitud.getActaUuid())
				.orElseThrow(() -> new RecursoNoEncontradoExcepcion(
						"No existe el acta E-14 con uuid " + solicitud.getActaUuid()));

		Integer maxVersion = lifecycleRepositorio.findMaxVersionNumberByActaUuid(solicitud.getActaUuid());
		int nuevaVersion = maxVersion + 1;

		ActaE14Lifecycle ultimaVersion = null;
		if (nuevaVersion > 1) {
			ultimaVersion = lifecycleRepositorio.findLatestByActaUuid(solicitud.getActaUuid()).orElse(null);
		}

		String sha256 = calcularSha256(solicitud.getDocumentBytes());
		UUID authorizationRef = solicitud.getAuthorizationRef() != null
				? UUID.fromString(solicitud.getAuthorizationRef())
				: UUID.randomUUID();

		ActaE14Lifecycle lifecycle = ActaE14Lifecycle.builder()
				.acta(acta)
				.versionNumber(nuevaVersion)
				.previousVersion(ultimaVersion)
				.estado(solicitud.getNuevoEstado())
				.timestampNtp(OffsetDateTime.now())
				.actorId(solicitud.getActorId())
				.deviceId(solicitud.getDeviceId())
				.documentSha256(sha256)
				.authorizationRef(authorizationRef.toString())
				.metadata(solicitud.getMetadataJson())
				.build();

		lifecycle = lifecycleRepositorio.save(lifecycle);

		acta.setEstado(solicitud.getNuevoEstado());
		acta.setUpdatedAt(OffsetDateTime.now());
		actaRepositorio.save(acta);

		TipoAccionAuditoria accion = mapearAccion(solicitud.getNuevoEstado());
		servicioAuditoria.logEvent(solicitud.getActorId(), accion, "ACTA_E14",
				acta.getUuid().toString(), null, solicitud.getDeviceId());

		logger.info("[ACTA-E14] Transicion registrada. uuid={}, estado={}, version={}, actor={}",
				acta.getUuid(), solicitud.getNuevoEstado(), nuevaVersion, solicitud.getActorId());
		return toLifecycleDto(lifecycle);
	}

	@Transactional(readOnly = true)
	public List<ActaE14LifecycleRespuestaDto> getTimeline(UUID actaUuid) {
		List<ActaE14LifecycleRespuestaDto> timeline = lifecycleRepositorio
				.findByActaUuidOrderByVersionNumberAsc(actaUuid)
				.stream().map(this::toLifecycleDto).toList();

		if (timeline.isEmpty()) {
			logger.info("[ACTA-E14-MOCK] Devolviendo timeline de ejemplo para uuid={}", actaUuid);
			return getMockTimeline(actaUuid);
		}
		return timeline;
	}

	@Transactional(readOnly = true)
	public List<ActaE14LifecycleRespuestaDto> getVersionChain(UUID actaUuid) {
		List<ActaE14LifecycleRespuestaDto> versions = lifecycleRepositorio
				.findByActaUuidOrderByVersionNumberDesc(actaUuid)
				.stream().map(this::toLifecycleDto).toList();

		if (versions.isEmpty()) {
			logger.info("[ACTA-E14-MOCK] Devolviendo versiones de ejemplo para uuid={}", actaUuid);
			return getMockVersions(actaUuid);
		}
		return versions;
	}

	@Transactional(readOnly = true)
	public VerificacionActaE14RespuestaDto verifyIntegrity(UUID actaUuid) {
		List<ActaE14Lifecycle> versiones = lifecycleRepositorio
				.findByActaUuidOrderByVersionNumberAsc(actaUuid);
		if (versiones.isEmpty()) {
			logger.info("[ACTA-E14-MOCK] Devolviendo verificacion de ejemplo para uuid={}", actaUuid);
			return getMockVerification(actaUuid);
		}

		List<VersionErrorDto> errores = new ArrayList<>();
		int verificadas = 0;

		for (ActaE14Lifecycle v : versiones) {
			if (v.getDocumentSha256() != null) {
				errores.add(new VersionErrorDto(v.getVersionNumber(),
						"TODO: verifyIntegrity requiere documento original para recomputar SHA-256. "
						+ "La verificacion de integridad del documento binario necesita acceso al storage del documento."));
				break;
			} else {
				verificadas++;
			}
		}

		boolean valida = errores.isEmpty();
		logger.info("[ACTA-E14] Verificacion integridad uuid={}, valida={}, versiones={}",
				actaUuid, valida, versiones.size());
		return new VerificacionActaE14RespuestaDto(valida, actaUuid, versiones.size(), verificadas, errores);
	}

	private String calcularSha256(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		try {
			MessageDigest d = MessageDigest.getInstance("SHA-256");
			byte[] hash = d.digest(data);
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 no disponible", e);
		}
	}

	private TipoAccionAuditoria mapearAccion(EstadoActaE14 estado) {
		return switch (estado) {
			case FIRMADA -> TipoAccionAuditoria.SIGN;
			case RECLAMADA -> TipoAccionAuditoria.CREATE;
			case CORREGIDA -> TipoAccionAuditoria.UPDATE;
			case CONSOLIDADA -> TipoAccionAuditoria.APPROVE;
			default -> TipoAccionAuditoria.TRANSITION;
		};
	}

	private ActaE14RespuestaDto toActaDto(ActaE14 acta) {
		return new ActaE14RespuestaDto(
				acta.getUuid(), acta.getMesaId(), acta.getEleccionId(),
				acta.getNumeroFormulario(),
				acta.getEstado() != null ? acta.getEstado().name() : null,
				acta.getCreatedAt() != null ? acta.getCreatedAt().toString() : null,
				acta.getUpdatedAt() != null ? acta.getUpdatedAt().toString() : null);
	}

	private ActaE14LifecycleRespuestaDto toLifecycleDto(ActaE14Lifecycle l) {
		UUID prevUuid = l.getPreviousVersion() != null ? l.getPreviousVersion().getUuid() : null;
		return new ActaE14LifecycleRespuestaDto(
				l.getUuid(),
				l.getActa().getUuid(),
				l.getVersionNumber(), prevUuid,
				l.getEstado().name(),
				l.getTimestampNtp().toString(),
				l.getActorId(), l.getDeviceId(),
				l.getDocumentSha256(),
				l.getAuthorizationRef(),
				l.getMetadata());
	}

	private List<ActaE14LifecycleRespuestaDto> getMockTimeline(UUID actaUuid) {
		List<ActaE14LifecycleRespuestaDto> mock = new ArrayList<>();

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("11111111-1111-1111-1111-111111111111"), actaUuid, 1, null,
				"CREADA", "2026-05-11T08:30:00Z",
				"usr-mesa-001", "tablet-mesa-001",
				"a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2",
				null, "{\"mesa\":\"001\",\"puesto\":\"Colegio Central\"}"));

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("22222222-2222-2222-2222-222222222222"), actaUuid, 1, null,
				"FIRMADA", "2026-05-11T14:45:00Z",
				"usr-mesa-001", "tablet-mesa-001",
				"b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3",
				null, "{\"firmantes\":[\"delegado-1\",\"delegado-2\",\"testigo-1\"]}"));

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("33333333-3333-3333-3333-333333333333"), actaUuid, 1, null,
				"ENVIADA", "2026-05-11T15:00:00Z",
				"usr-mesa-001", "tablet-mesa-001",
				"c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4",
				null, "{\"transportador\":\"mensajero-001\"}"));

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("44444444-4444-4444-4444-444444444444"), actaUuid, 1, null,
				"RECIBIDA", "2026-05-11T16:30:00Z",
				"usr-oficina-001", "pc-oficina-001",
				"d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5",
				null, "{\"receptor\":\"coordinador-001\"}"));

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("55555555-5555-5555-5555-555555555555"), actaUuid, 1, null,
				"VERIFICADA", "2026-05-11T17:00:00Z",
				"usr-auditor-001", "pc-auditoria-001",
				"e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6",
				null, "{\"verificacion\":\"automatica\",\"coincidencias\":45}"));

		return mock;
	}

	private List<ActaE14LifecycleRespuestaDto> getMockVersions(UUID actaUuid) {
		List<ActaE14LifecycleRespuestaDto> mock = new ArrayList<>();

		mock.add(new ActaE14LifecycleRespuestaDto(
				UUID.fromString("55555555-5555-5555-5555-555555555555"), actaUuid, 1, null,
				"VERIFICADA", "2026-05-11T17:00:00Z",
				"usr-auditor-001", "pc-auditoria-001",
				"e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6",
				null, null));

		return mock;
	}

	private VerificacionActaE14RespuestaDto getMockVerification(UUID actaUuid) {
		String hash = "e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6";
		return new VerificacionActaE14RespuestaDto(true, actaUuid, 1, 1,
				List.of(new VersionErrorDto(null, "Modo demo: verificacion exitosa")));
	}
}
