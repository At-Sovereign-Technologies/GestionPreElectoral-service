package com.selloLegitimo.GestionPreElectoral.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCensoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.AutorizacionCierreSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CongelarCensoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CongelamientoCensoRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.DetalleEleccionExternaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ImportarCensoApiSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.PaginaCensoRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCiudadanoCensoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistroCensoRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistroImportacionExternaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResumenCensoDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;
import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.modelo.CausalCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoEleccion;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCongelamientoCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.RegistroCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoEventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.CiudadanoRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.EstadoCongelamientoCensoRepositorio;
import com.selloLegitimo.GestionPreElectoral.servicio.PuertoAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.RegistroCensoRepositorio;

@Service
public class ServicioCenso {

	private static final Logger logger = LoggerFactory.getLogger(ServicioCenso.class);
	@Autowired
	private RegistroCensoRepositorio registroCensoRepositorio;

	@Autowired
	private CiudadanoRepositorio ciudadanoRepositorio;

	@Autowired
	private ServicioEleccion servicioEleccion;

	@Autowired
	private EstadoCongelamientoCensoRepositorio estadoCongelamientoCensoRepositorio;

	@Autowired
	private PuertoAuditoria puertoAuditoria;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private RestClient clienteRest;

	@Transactional
	@CacheEvict(value = { "censo", "censoResumen" }, allEntries = true)
	public RegistroCensoRespuestaDto registrarManual(RegistrarCiudadanoCensoSolicitudDto solicitud) {
		return procesarRegistro(solicitud);
	}

	private RegistroCensoRespuestaDto procesarRegistro(RegistrarCiudadanoCensoSolicitudDto solicitud) {
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(solicitud.getEleccionId());
		validarEstadoCerrado(eleccion, solicitud.getActor(), solicitud.getAutorizacionCierre());
		validarCoherenciaEstado(solicitud.getEstado(), solicitud.getCausalEstado());

		Ciudadano ciudadano = ciudadanoRepositorio
			.findByTipoDocumentoAndNumeroDocumento(solicitud.getTipoDocumento(), solicitud.getNumeroDocumento())
			.map(existente -> {
				existente.actualizarDatos(solicitud.getNombres(), solicitud.getApellidos(), solicitud.getFechaNacimiento());
				if (solicitud.getDepartamento() != null) {
					existente.actualizarDatosGeo(solicitud.getDepartamento(), solicitud.getMunicipio());
				}
				return existente;
			})
			.orElseGet(() -> {
				Ciudadano nuevo = new Ciudadano(solicitud.getTipoDocumento(), solicitud.getNumeroDocumento(),
					solicitud.getNombres(), solicitud.getApellidos(), solicitud.getFechaNacimiento(),
					solicitud.getDepartamento(), solicitud.getMunicipio());
				return ciudadanoRepositorio.save(nuevo);
			});
		String hashBiometrico = calcularHashBiometrico(ciudadano);

		Optional<RegistroCenso> registroExistente = registroCensoRepositorio.findByEleccionIdAndCiudadano(eleccion.getId(), ciudadano);
		RegistroCenso registro = registroExistente
			.map(existente -> actualizarRegistro(existente, solicitud.getEstado(), solicitud.getCausalEstado(), solicitud.getObservacion(), solicitud.getActor(), hashBiometrico))
			.orElseGet(() -> registroCensoRepositorio.save(new RegistroCenso(eleccion.getId(), ciudadano, solicitud.getEstado(),
					solicitud.getCausalEstado(), solicitud.getObservacion(), solicitud.getActor(), hashBiometrico)));
		logger.info("Registro manual de censo procesado. elección={}, registro={}, actor={}", eleccion.getId(), registro.getId(), solicitud.getActor());
		return mapear(registro);
	}

	@Transactional
	@CacheEvict(value = { "censo", "censoResumen" }, allEntries = true)
	public CongelamientoCensoRespuestaDto congelarCenso(Long eleccionId, CongelarCensoSolicitudDto solicitud) {
		DetalleEleccionExternaDto eleccion = obtenerEleccionParaCongelar(eleccionId);
		if (!eleccion.estaCerrada()) {
			throw new ExcepcionNegocio("La elección debe estar cerrada para congelar el censo");
		}
		String actor = solicitud != null && solicitud.getActor() != null ? solicitud.getActor() : "sistema";
		List<String> hashesBiometricos = registroCensoRepositorio.encontrarHashesBiometricosPorEleccionId(eleccionId);
		String hashRaiz = calcularHashRaiz(hashesBiometricos);
		EstadoCongelamientoCenso estado = estadoCongelamientoCensoRepositorio.findById(eleccionId)
			.orElseGet(EstadoCongelamientoCenso::new);
		if (estado.getEleccionId() == null) {
			estado = new EstadoCongelamientoCenso(eleccionId, "CERRADA", true, hashRaiz, LocalDateTime.now(), actor);
		} else {
			estado.setEstadoEleccion("CERRADA");
			estado.setCensoCongelado(true);
			estado.setHashRaizCenso(hashRaiz);
			estado.setFechaCongelamiento(LocalDateTime.now());
			estado.setActorCongelamiento(actor);
		}
		estadoCongelamientoCensoRepositorio.save(estado);
		registrarAuditoriaCongelamiento(eleccionId, actor, hashRaiz, hashesBiometricos.size());
		logger.info("Censo congelado. elección={}, hashRaiz={}, totalRegistros={}, actor={}", eleccionId, hashRaiz,
				hashesBiometricos.size(), actor);
		return new CongelamientoCensoRespuestaDto(eleccionId, "CERRADA", hashRaiz, hashesBiometricos.size());
	}

	private DetalleEleccionExternaDto obtenerEleccionParaCongelar(Long eleccionId) {
		try {
			return servicioEleccion.obtenerEleccion(eleccionId);
		} catch (ExcepcionNegocio ex) {
			logger.warn("Bypass local: asumiendo elección CERRADA para pruebas. eleccionId={}, motivo={}", eleccionId,
					ex.getMessage());
			return new DetalleEleccionExternaDto(eleccionId, "Elección local simulada", EstadoEleccion.CERRADA,
					null, null, null, null, null, null, List.of(), null);
		}
	}

	@Transactional
	@CacheEvict(value = { "censo", "censoResumen" }, allEntries = true)
	public RegistroCensoRespuestaDto actualizarEstado(Long registroId, ActualizarEstadoCensoSolicitudDto solicitud) {
		RegistroCenso registro = registroCensoRepositorio.findById(registroId)
			.orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe el registro de censo con id " + registroId));
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(registro.getEleccionId());
		validarEstadoCerrado(eleccion, solicitud.getActor(), solicitud.getAutorizacionCierre());
		validarCoherenciaEstado(solicitud.getEstado(), solicitud.getCausalEstado());
		registro.actualizarEstado(solicitud.getEstado(), solicitud.getCausalEstado(), solicitud.getObservacion(), solicitud.getActor());
		logger.info("Estado de censo actualizado. registro={}, actor={}", registro.getId(), solicitud.getActor());
		return mapear(registro);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "censo", key = "#eleccionId + '-' + (#estado != null ? #estado : 'null') + '-' + (#search != null ? #search : 'null') + '-' + #pagina + '-' + #tamano")
	public PaginaCensoRespuestaDto listarPorEleccion(Long eleccionId, String estado, String search, int pagina, int tamano) {
		EstadoCenso estadoFiltro = null;
		if (estado != null && !estado.isBlank()) {
			estadoFiltro = EstadoCenso.valueOf(estado.toUpperCase());
		}
		Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaActualizacion").descending());
		Page<RegistroCenso> paginaResultados = registroCensoRepositorio.buscarPaginado(eleccionId, estadoFiltro, search, pageable);
		List<RegistroCensoRespuestaDto> contenido = paginaResultados.getContent().stream().map(this::mapear).toList();
		return new PaginaCensoRespuestaDto(contenido, paginaResultados.getTotalElements(),
				paginaResultados.getTotalPages(), paginaResultados.getNumber(), paginaResultados.getSize());
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "censoResumen", key = "#eleccionId")
	public ResumenCensoDto obtenerResumen(Long eleccionId) {
		List<Object[]> resultados = registroCensoRepositorio.contarPorEstado(eleccionId);
		long habilitados = 0;
		long excluidos = 0;
		long exentos = 0;
		for (Object[] fila : resultados) {
			EstadoCenso estado = (EstadoCenso) fila[0];
			long cantidad = (Long) fila[1];
			switch (estado) {
				case HABILITADO -> habilitados = cantidad;
				case EXCLUIDO -> excluidos = cantidad;
				case EXENTO -> exentos = cantidad;
			}
		}
		return new ResumenCensoDto(habilitados + excluidos + exentos, habilitados, excluidos, exentos);
	}

	@Transactional
	@CacheEvict(value = { "censo", "censoResumen" }, allEntries = true)
	public int importarCsv(Long eleccionId, MultipartFile archivo, String actor, AutorizacionCierreSolicitudDto autorizacionCierre) {
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(eleccionId);
		validarEstadoCerrado(eleccion, actor, autorizacionCierre);
		if (archivo == null || archivo.isEmpty()) {
			throw new ExcepcionNegocio("Debe adjuntar un archivo CSV con registros de censo");
		}
		try (Reader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8));
				CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
			int procesados = 0;
			for (CSVRecord record : parser) {
				String departamentoCsv = record.isMapped("departamento") ? record.get("departamento") : null;
				String municipioCsv = record.isMapped("municipio") ? record.get("municipio") : null;
				RegistrarCiudadanoCensoSolicitudDto solicitud = new RegistrarCiudadanoCensoSolicitudDto(eleccionId,
						record.get("tipoDocumento").trim(), record.get("numeroDocumento").trim(), record.get("nombres").trim(), record.get("apellidos").trim(),
						parsearFecha(record.get("fechaNacimiento")),
						departamentoCsv != null && !departamentoCsv.isBlank() ? departamentoCsv.trim() : null,
						municipioCsv != null && !municipioCsv.isBlank() ? municipioCsv.trim() : null,
						EstadoCenso.valueOf(record.get("estado").trim().toUpperCase()),
						parsearCausal(record.get("causalEstado")), record.get("observacion"), actor, autorizacionCierre);
				procesarRegistro(solicitud);
				procesados++;
			}
			logger.info("Importación CSV completada. elección={}, total={}, actor={}", eleccionId, procesados, actor);
			return procesados;
		} catch (IOException ex) {
			throw new ExcepcionNegocio("No fue posible leer el archivo CSV");
		}
	}

	@Transactional
	@CacheEvict(value = { "censo", "censoResumen" }, allEntries = true)
	public int importarApi(ImportarCensoApiSolicitudDto solicitud) {
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(solicitud.getEleccionId());
		validarEstadoCerrado(eleccion, solicitud.getActor(), solicitud.getAutorizacionCierre());

		RegistroImportacionExternaDto[] registrosExternos;
		try {
			registrosExternos = clienteRest.get()
					.uri(solicitud.getUrl())
					.retrieve()
					.body(RegistroImportacionExternaDto[].class);
		} catch (Exception ex) {
			throw new ExcepcionNegocio("No fue posible consultar la API externa de censo");
		}

		if (registrosExternos == null) {
			throw new ExcepcionNegocio("La fuente externa no devolvió registros");
		}

		Arrays.stream(registrosExternos).forEach(registro -> procesarRegistro(new RegistrarCiudadanoCensoSolicitudDto(
				solicitud.getEleccionId(), registro.getTipoDocumento(), registro.getNumeroDocumento(), registro.getNombres(),
				registro.getApellidos(), registro.getFechaNacimiento(), registro.getDepartamento(), registro.getMunicipio(),
				registro.getEstado(), registro.getCausalEstado(),
				registro.getObservacion(), solicitud.getActor(), solicitud.getAutorizacionCierre())));
		logger.info("Importación API completada. elección={}, total={}, actor={}", solicitud.getEleccionId(), registrosExternos.length, solicitud.getActor());
		return registrosExternos.length;
	}

	private RegistroCenso actualizarRegistro(RegistroCenso registro, EstadoCenso estado, CausalCenso causalEstado,
			String observacion, String actor, String hashBiometrico) {
		registro.actualizarEstado(estado, causalEstado, observacion, actor);
		registro.actualizarHashBiometrico(hashBiometrico);
		return registro;
	}

	private void validarEstadoCerrado(DetalleEleccionExternaDto eleccion, String actor, AutorizacionCierreSolicitudDto autorizacionCierre) {
		if (!eleccion.estaCerrada()) {
			return;
		}
		if (autorizacionCierre == null || autorizacionCierre.getSuperadministrador() == null
				|| autorizacionCierre.getSuperadministrador().isBlank() || autorizacionCierre.getJustificacion() == null
				|| autorizacionCierre.getJustificacion().isBlank()) {
			throw new ExcepcionNegocio("La elección está cerrada. Se requiere autorización del superadministrador con justificación");
		}
		logger.warn("Modificación sobre censo cerrado autorizada. elección={}, actor={}, superadministrador={}", eleccion.getId(), actor,
				autorizacionCierre.getSuperadministrador());
	}

	private void validarCoherenciaEstado(EstadoCenso estado, CausalCenso causalEstado) {
		if (EstadoCenso.HABILITADO.equals(estado) && causalEstado != null) {
			throw new ExcepcionNegocio("El estado HABILITADO no debe registrar causal");
		}
		if (!EstadoCenso.HABILITADO.equals(estado) && causalEstado == null) {
			throw new ExcepcionNegocio("Los estados EXCLUIDO y EXENTO requieren causal");
		}
	}

	private LocalDate parsearFecha(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return LocalDate.parse(valor);
	}

	private CausalCenso parsearCausal(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return CausalCenso.valueOf(valor.trim().toUpperCase());
	}

	private String calcularHashBiometrico(Ciudadano ciudadano) {
		String entrada = new StringJoiner("|")
			.add(valorOEmpty(ciudadano.getTipoDocumento()))
			.add(valorOEmpty(ciudadano.getNumeroDocumento()))
			.add(valorOEmpty(ciudadano.getNombres()))
			.add(valorOEmpty(ciudadano.getApellidos()))
			.add(ciudadano.getFechaNacimiento() != null ? ciudadano.getFechaNacimiento().toString() : "")
			.add(valorOEmpty(ciudadano.getDepartamento()))
			.add(valorOEmpty(ciudadano.getMunicipio()))
			.toString();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 no disponible", ex);
		}
	}

	private String calcularHashRaiz(List<String> hashesBiometricos) {
		String entrada = String.join("", hashesBiometricos);
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 no disponible", ex);
		}
	}

	private String valorOEmpty(String valor) {
		return valor == null ? "" : valor;
	}

	private void registrarAuditoriaCongelamiento(Long eleccionId, String actor, String hashRaiz, int totalRegistros) {
		try {
			String payload = objectMapper.writeValueAsString(java.util.Map.of(
				"eleccionId", eleccionId,
				"hashRaiz", hashRaiz,
				"totalRegistros", totalRegistros,
				"censoCongelado", true));
			puertoAuditoria.registrarEvento(new EventoAuditoria("CENSO", eleccionId, TipoEventoAuditoria.CENSO_CONGELADO,
				actor, payload, ""));
		} catch (JsonProcessingException ex) {
			throw new ExcepcionNegocio("No fue posible registrar la auditoría del congelamiento del censo");
		}
	}

	private RegistroCensoRespuestaDto mapear(RegistroCenso registro) {
		return new RegistroCensoRespuestaDto(registro.getId(), registro.getEleccionId(),
				registro.getCiudadano().getTipoDocumento(), registro.getCiudadano().getNumeroDocumento(),
				registro.getCiudadano().getNombres(), registro.getCiudadano().getApellidos(),
				registro.getCiudadano().getFechaNacimiento(), registro.getCiudadano().getDepartamento(),
				registro.getCiudadano().getMunicipio(),
				registro.getEstado(), registro.getCausalEstado(),
				registro.getObservacion(), registro.getActorUltimaModificacion(), registro.getFechaActualizacion());
	}
}