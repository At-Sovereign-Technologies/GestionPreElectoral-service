package com.selloLegitimo.GestionPreElectoral.controlador;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.AuditLogFilterDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistroAuditoriaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificacionCadenaAuditoriaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioAuditoria;

@Validated
@RestController
@RequestMapping("/api/auditoria")
public class ControladorAuditoria {

	@Autowired
	private ServicioAuditoria servicioAuditoria;

	@GetMapping
	public Page<RegistroAuditoriaRespuestaDto> listar(
			@RequestParam(required = false) String actorId,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) OffsetDateTime inicio,
			@RequestParam(required = false) OffsetDateTime fin,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "50") int tamano) {

		AuditLogFilterDto filtro = new AuditLogFilterDto();
		filtro.setActorId(actorId);
		filtro.setEntityType(entityType);
		filtro.setInicio(inicio);
		filtro.setFin(fin);

		PageRequest pageRequest = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "timestampNtp"));
		return servicioAuditoria.filtrar(filtro, pageRequest);
	}

	@GetMapping("/exportar")
	public ResponseEntity<byte[]> exportar(
			@RequestParam(required = false) String actorId,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) OffsetDateTime inicio,
			@RequestParam(required = false) OffsetDateTime fin,
			@RequestParam(defaultValue = "csv") String formato) {

		AuditLogFilterDto filtro = new AuditLogFilterDto();
		filtro.setActorId(actorId);
		filtro.setEntityType(entityType);
		filtro.setInicio(inicio);
		filtro.setFin(fin);

		PageRequest pageRequest = PageRequest.of(0, 10000, Sort.by(Sort.Direction.ASC, "id"));
		Page<RegistroAuditoriaRespuestaDto> page = servicioAuditoria.filtrar(filtro, pageRequest);
		List<RegistroAuditoriaRespuestaDto> registros = page.getContent();

		String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
		byte[] body;

		if ("json".equalsIgnoreCase(formato)) {
			body = serializeToJson(registros);
		} else {
			body = serializeToCsv(registros);
		}

		String contentType = "json".equalsIgnoreCase(formato)
				? MediaType.APPLICATION_JSON_VALUE
				: "text/csv";

		String filename = "audit_log_export_" + timestamp + "." + formato.toLowerCase();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(MediaType.parseMediaType(contentType))
				.body(body);
	}

	@GetMapping("/verificar-cadena")
	public VerificacionCadenaAuditoriaRespuestaDto verificarCadena() {
		return servicioAuditoria.verifyChainIntegrity();
	}

	@GetMapping("/entidad")
	public List<RegistroAuditoriaRespuestaDto> porEntidad(
			@RequestParam String entityType,
			@RequestParam String entityId) {
		return servicioAuditoria.getEventsByEntity(entityType, entityId);
	}

	private byte[] serializeToCsv(List<RegistroAuditoriaRespuestaDto> registros) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (PrintWriter writer = new PrintWriter(out);
				CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
						.setHeader("id", "event_id", "actor_id", "action", "entity_type",
								"entity_id", "timestamp_ntp", "ip_address", "device_id",
								"payload_hash", "chain_hash").build())) {

			for (RegistroAuditoriaRespuestaDto r : registros) {
				csv.printRecord(r.getId(), r.getEventId(), r.getActorId(), r.getAction(),
						r.getEntityType(), r.getEntityId(), r.getTimestampNtp(),
						r.getIpAddress(), r.getDeviceId(), r.getPayloadHash(), r.getChainHash());
			}
		} catch (java.io.IOException e) {
			throw new RuntimeException("Error generando CSV", e);
		}
		return out.toByteArray();
	}

	private byte[] serializeToJson(List<RegistroAuditoriaRespuestaDto> registros) {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for (int i = 0; i < registros.size(); i++) {
			RegistroAuditoriaRespuestaDto r = registros.get(i);
			sb.append("  {\n");
			sb.append("    \"id\": ").append(r.getId()).append(",\n");
			sb.append("    \"eventId\": \"").append(r.getEventId()).append("\",\n");
			sb.append("    \"actorId\": \"").append(escapeJson(r.getActorId())).append("\",\n");
			sb.append("    \"action\": \"").append(r.getAction()).append("\",\n");
			sb.append("    \"entityType\": \"").append(escapeJson(r.getEntityType())).append("\",\n");
			sb.append("    \"entityId\": \"").append(escapeJson(r.getEntityId())).append("\",\n");
			sb.append("    \"timestampNtp\": \"").append(r.getTimestampNtp()).append("\",\n");
			sb.append("    \"ipAddress\": \"").append(nvl(r.getIpAddress())).append("\",\n");
			sb.append("    \"deviceId\": \"").append(nvl(r.getDeviceId())).append("\",\n");
			sb.append("    \"payloadHash\": \"").append(r.getPayloadHash()).append("\",\n");
			sb.append("    \"chainHash\": \"").append(r.getChainHash()).append("\"\n");
			sb.append("  }");
			if (i < registros.size() - 1) sb.append(",");
			sb.append("\n");
		}
		sb.append("]\n");
		return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}

	private String escapeJson(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"")
				.replace("\n", "\\n").replace("\r", "\\r");
	}

	private String nvl(String s) {
		return s != null ? s : "";
	}
}
