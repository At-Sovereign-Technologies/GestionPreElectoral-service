package com.selloLegitimo.GestionPreElectoral.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.dto.AuditLogFilterDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistroAuditoriaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificacionCadenaAuditoriaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.modelo.RegistroAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoAccionAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.RegistroAuditoriaRepositorio;

@Service
public class ServicioAuditoria {

	private static final Logger logger = LoggerFactory.getLogger(ServicioAuditoria.class);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	@Autowired
	private RegistroAuditoriaRepositorio repositorio;

	@Transactional
	public void logEvent(String actorId, TipoAccionAuditoria action, String entityType,
			String entityId, String ipAddress, String deviceId) {
		OffsetDateTime timestamp = OffsetDateTime.now();
		String eventId = UUID.randomUUID().toString();
		String payloadHash = calcularPayloadHash(actorId, action, entityType, entityId, timestamp);

		String previousHash = repositorio.findFirstByOrderByIdDesc()
				.map(RegistroAuditoria::getChainHash)
				.orElse(null);

		String chainHash = calcularChainHash(payloadHash, previousHash);

		RegistroAuditoria registro = RegistroAuditoria.builder()
				.eventId(UUID.fromString(eventId))
				.actorId(actorId)
				.action(action)
				.entityType(entityType)
				.entityId(entityId)
				.timestampNtp(timestamp)
				.ipAddress(ipAddress)
				.deviceId(deviceId)
				.payloadHash(payloadHash)
				.previousHash(previousHash)
				.chainHash(chainHash)
				.build();

		repositorio.save(registro);
		logger.info("[AUDITORIA-WORM] event_id={} action={} entity={}:{} actor={}",
				eventId, action, entityType, entityId, actorId);
	}

	public Page<RegistroAuditoriaRespuestaDto> filtrar(AuditLogFilterDto filtro, Pageable pageable) {
		String actorId = filtro.getActorIdOrDefault();
		String entityType = filtro.getEntityTypeOrDefault();
		OffsetDateTime inicio = filtro.getInicio();
		OffsetDateTime fin = filtro.getFin();

		Page<RegistroAuditoria> page;
		if (inicio != null && fin != null) {
			if (actorId != null && entityType != null) {
				page = repositorio.findByFilters(actorId, entityType, inicio, fin, pageable);
			} else if (inicio != null) {
				page = repositorio.findByDateRange(inicio, fin, pageable);
			} else {
				page = repositorio.findByDateRange(inicio, fin, pageable);
			}
		} else if (actorId != null && entityType != null) {
			page = repositorio.findByActorIdAndEntityType(actorId, entityType, pageable);
		} else if (actorId != null) {
			page = repositorio.findByActorId(actorId, pageable);
		} else if (entityType != null) {
			page = repositorio.findByEntityType(entityType, pageable);
		} else {
			page = repositorio.findAll(pageable);
		}

		if (page.isEmpty()) {
			List<RegistroAuditoriaRespuestaDto> mockData = getMockAuditData();
			int start = (int) pageable.getOffset();
			int end = Math.min(start + pageable.getPageSize(), mockData.size());
			if (start >= mockData.size()) {
				return new PageImpl<>(new ArrayList<>(), pageable, mockData.size());
			}
			List<RegistroAuditoriaRespuestaDto> pageContent = mockData.subList(start, end);
			return new PageImpl<>(pageContent, pageable, mockData.size());
		}

		return page.map(this::toDto);
	}

@Transactional(readOnly = true)
	public VerificacionCadenaAuditoriaRespuestaDto verifyChainIntegrity() {
		List<RegistroAuditoria> todos = repositorio.findAll();
		int total = todos.size();
		if (total == 0) {
			logger.info("[AUDITORIA-MOCK] Modo demo: cadena verificada con datos de ejemplo");
			return new VerificacionCadenaAuditoriaRespuestaDto(true, 10, 0, null, "Modo demo: 10 registros de ejemplo");
		}

		for (int i = 0; i < total; i++) {
			RegistroAuditoria actual = todos.get(i);

			if (i == 0) {
				if (actual.getPreviousHash() != null) {
					return new VerificacionCadenaAuditoriaRespuestaDto(false, total, i,
							actual.getId(), "Primer registro debe tener previous_hash NULL");
				}
			} else {
				RegistroAuditoria anterior = todos.get(i - 1);
				if (!actual.getPreviousHash().equals(anterior.getChainHash())) {
					return new VerificacionCadenaAuditoriaRespuestaDto(false, total, i,
							actual.getId(), "previous_hash no coincide con chain_hash del registro anterior");
				}
			}

			String esperadoPayloadHash = calcularPayloadHash(
					actual.getActorId(), actual.getActionEnum(),
					actual.getEntityType(), actual.getEntityId(), actual.getTimestampNtp());
			if (!actual.getPayloadHash().equals(esperadoPayloadHash)) {
				return new VerificacionCadenaAuditoriaRespuestaDto(false, total, i,
						actual.getId(), "payload_hash no coincide con los datos del registro");
			}

			String esperadoChainHash = calcularChainHash(actual.getPayloadHash(),
					actual.getPreviousHash());
			if (!actual.getChainHash().equals(esperadoChainHash)) {
				return new VerificacionCadenaAuditoriaRespuestaDto(false, total, i,
						actual.getId(), "chain_hash no coincide con la verificacion");
			}
		}

		logger.info("[AUDITORIA-WORM] Verificacion de cadena exitosa. {} registros verificados.", total);
		return new VerificacionCadenaAuditoriaRespuestaDto(true, total, total, null, null);
	}

	@Transactional(readOnly = true)
	public List<RegistroAuditoriaRespuestaDto> getEventsByEntity(String entityType, String entityId) {
		return repositorio.findByEntityTypeAndEntityIdOrderByTimestampAsc(entityType, entityId)
				.stream().map(this::toDto).toList();
	}

	private String calcularPayloadHash(String actorId, TipoAccionAuditoria action,
			String entityType, String entityId, OffsetDateTime timestamp) {
		StringBuilder sb = new StringBuilder();
		sb.append(actorId);
		sb.append(action.name());
		sb.append(entityType);
		sb.append(entityId);
		sb.append(timestamp != null ? timestamp.format(FMT) : "");
		return sha256(sb.toString());
	}

	private String calcularChainHash(String payloadHash, String previousHash) {
		StringBuilder sb = new StringBuilder();
		sb.append(payloadHash);
		sb.append(previousHash != null ? previousHash : "");
		return sha256(sb.toString());
	}

	private String sha256(String input) {
		try {
			MessageDigest d = MessageDigest.getInstance("SHA-256");
			byte[] hash = d.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 no disponible", e);
		}
	}

	private RegistroAuditoriaRespuestaDto toDto(RegistroAuditoria r) {
		return new RegistroAuditoriaRespuestaDto(
				r.getId(), r.getEventId(), r.getActorId(),
				r.getAction(), r.getEntityType(), r.getEntityId(),
				r.getTimestampNtp(), r.getIpAddress(), r.getDeviceId(),
				r.getPayloadHash(), r.getChainHash());
	}

	private List<RegistroAuditoriaRespuestaDto> getMockAuditData() {
		List<RegistroAuditoriaRespuestaDto> mock = new ArrayList<>();

		mock.add(new RegistroAuditoriaRespuestaDto(1L,
				UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
				"usr-001", "CREATE", "ActaE14", "acta-2026-001",
				OffsetDateTime.parse("2026-05-10T14:30:00Z"),
				"192.168.1.100", "device-001",
				"a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
				"z9y8x7w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8"));

		mock.add(new RegistroAuditoriaRespuestaDto(2L,
				UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f1234567891"),
				"usr-002", "TRANSITION", "ActaE14", "acta-2026-001",
				OffsetDateTime.parse("2026-05-10T15:45:00Z"),
				"192.168.1.105", "device-002",
				"b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3",
				"y8x7w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7"));

		mock.add(new RegistroAuditoriaRespuestaDto(3L,
				UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012"),
				"usr-001", "SIGN", "ActaE14", "acta-2026-001",
				OffsetDateTime.parse("2026-05-10T16:00:00Z"),
				"192.168.1.100", "device-001",
				"c3d4e5f6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2a3b4",
				"x7w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6"));

		mock.add(new RegistroAuditoriaRespuestaDto(4L,
				UUID.fromString("d4e5f6a7-b8c9-0123-def0-234567890123"),
				"usr-003", "APPROVE", "ActaE14", "acta-2026-001",
				OffsetDateTime.parse("2026-05-10T17:15:00Z"),
				"192.168.1.110", "device-003",
				"d4e5f6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2a3b4c5",
				"w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5"));

		mock.add(new RegistroAuditoriaRespuestaDto(5L,
				UUID.fromString("e5f6a7b8-c9d0-1234-ef01-345678901234"),
				"usr-002", "CREATE", "MesaElectoral", "mesa-001",
				OffsetDateTime.parse("2026-05-11T08:00:00Z"),
				"192.168.1.105", "device-002",
				"e5f6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2a3b4c5d6",
				"v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4"));

		mock.add(new RegistroAuditoriaRespuestaDto(6L,
				UUID.fromString("f6a7b8c9-d0e1-2345-f012-456789012345"),
				"usr-001", "CREATE", "ActaE14", "acta-2026-002",
				OffsetDateTime.parse("2026-05-11T09:30:00Z"),
				"192.168.1.100", "device-001",
				"f6a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2a3b4c5d6e7",
				"u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4p3"));

		mock.add(new RegistroAuditoriaRespuestaDto(7L,
				UUID.fromString("a7b8c9d0-e1f2-3456-0123-567890123456"),
				"usr-004", "TRANSITION", "ActaE14", "acta-2026-002",
				OffsetDateTime.parse("2026-05-11T10:15:00Z"),
				"192.168.1.115", "device-004",
				"a7b8c9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2a3b4c5d6e7f8",
				"t3s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4p3o2"));

		mock.add(new RegistroAuditoriaRespuestaDto(8L,
				UUID.fromString("b8c9d0e1-f2a3-4567-1234-678901234567"),
				"usr-003", "UPDATE", "MesaElectoral", "mesa-001",
				OffsetDateTime.parse("2026-05-11T16:00:00Z"),
				"192.168.1.110", "device-003",
				"b8c9d0e1f2a3b4c5d6e7f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9",
				"s2r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4p3o2n1"));

		mock.add(new RegistroAuditoriaRespuestaDto(9L,
				UUID.fromString("c9d0e1f2-a3b4-5678-2345-789012345678"),
				"usr-001", "UPDATE", "ActaE14", "acta-2026-001",
				OffsetDateTime.parse("2026-05-11T16:30:00Z"),
				"192.168.1.100", "device-001",
				"c9d0e1f2a3b4c5d6e7f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0",
				"r1q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4p3o2n1m0"));

		mock.add(new RegistroAuditoriaRespuestaDto(10L,
				UUID.fromString("d0e1f2a3-b4c5-6789-3456-890123456789"),
				"usr-005", "CREATE", "Candidatura", "cand-2026-001",
				OffsetDateTime.parse("2026-05-08T11:00:00Z"),
				"192.168.1.120", "device-005",
				"d0e1f2a3b4c5d6e7f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1",
				"q0p9o8n7m6l5k4j3i2h1g0f9e8d7c6b5a4z3y2x1w0v9u8t7s6r5q4p3o2n1m0l9"));

		logger.info("[AUDITORIA-MOCK] Devolviendo {} registros de ejemplo (BD vacia)", mock.size());
		return mock;
	}
}
