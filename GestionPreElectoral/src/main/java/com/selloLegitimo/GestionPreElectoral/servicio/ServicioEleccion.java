package com.selloLegitimo.GestionPreElectoral.servicio;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.dto.DetalleEleccionExternaDto;
import com.selloLegitimo.GestionPreElectoral.dto.EleccionResumenDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoEleccion;
import com.selloLegitimo.grpc.elecciones.EleccionDetalle;
import com.selloLegitimo.grpc.elecciones.EleccionServiceGrpc;
import com.selloLegitimo.grpc.elecciones.ListarEleccionesRequest;
import com.selloLegitimo.grpc.elecciones.ListarEleccionesResponse;
import com.selloLegitimo.grpc.elecciones.ObtenerEleccionRequest;

import io.grpc.StatusRuntimeException;

@Service
public class ServicioEleccion {

	private static final Logger logger = LoggerFactory.getLogger(ServicioEleccion.class);

	private final EleccionServiceGrpc.EleccionServiceBlockingStub eleccionStub;

	public ServicioEleccion(EleccionServiceGrpc.EleccionServiceBlockingStub eleccionStub) {
		this.eleccionStub = eleccionStub;
	}

	@Transactional(readOnly = true)
	public List<EleccionResumenDto> listarElecciones() {
		logger.info("Consultando listado de elecciones por gRPC");

		try {
			ListarEleccionesResponse respuesta = eleccionStub
					.listarElecciones(ListarEleccionesRequest.newBuilder().build());

			return respuesta.getEleccionesList().stream()
					.map(e -> new EleccionResumenDto(e.getId(), e.getNombreOficial(), e.getEstado()))
					.toList();
		} catch (StatusRuntimeException ex) {
			logger.error("Error gRPC al listar elecciones", ex);
			throw new ExcepcionNegocio("No fue posible consultar las elecciones configuradas para operar el censo");
		}
	}

	@Transactional(readOnly = true)
	public DetalleEleccionExternaDto obtenerEleccion(Long eleccionId) {
		logger.info("Consultando elección por gRPC. eleccionId={}", eleccionId);

		try {
			EleccionDetalle detalle = eleccionStub
					.obtenerEleccion(ObtenerEleccionRequest.newBuilder().setId(eleccionId).build());

			return new DetalleEleccionExternaDto(
					detalle.getId(),
					detalle.getNombreOficial(),
					mapearEstado(detalle.getEstado()),
					parseFecha(detalle.getFechaInicioJornada()),
					parseFecha(detalle.getFechaCierreJornada()),
					detalle.getDocumentoNoVotable(),
					null,
					null,
					null);
		} catch (StatusRuntimeException ex) {
			logger.error("Error gRPC al obtener elección {}", eleccionId, ex);
			throw new ExcepcionNegocio("No fue posible consultar la elección configurada para operar el censo");
		}
	}

	private LocalDateTime parseFecha(String fecha) {
		if (fecha == null || fecha.isBlank()) {
			return null;
		}
		try {
			return LocalDateTime.parse(fecha);
		} catch (Exception e) {
			return null;
		}
	}

	private EstadoEleccion mapearEstado(String estado) {
		if (estado == null || estado.isBlank()) {
			return EstadoEleccion.ABIERTA;
		}
		return "CERRADA".equals(estado.trim()) ? EstadoEleccion.CERRADA : EstadoEleccion.ABIERTA;
	}
}