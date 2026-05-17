package com.selloLegitimo.GestionPreElectoral.servicio;

import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.dto.AperturaMesaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.AperturaMesaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.JuradoMesaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.SincronizarJuradoMesaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionAccesoDenegado;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.modelo.JuradoMesa;
import com.selloLegitimo.GestionPreElectoral.repositorio.JuradoMesaRepositorio;

@Service
public class ServicioJuradoMesa {

	private final JuradoMesaRepositorio juradoMesaRepositorio;

	public ServicioJuradoMesa(JuradoMesaRepositorio juradoMesaRepositorio) {
		this.juradoMesaRepositorio = juradoMesaRepositorio;
	}

	@Transactional
	public List<JuradoMesaRespuestaDto> sincronizar(List<SincronizarJuradoMesaSolicitudDto> solicitudes) {
		if (solicitudes == null || solicitudes.isEmpty()) {
			throw new ExcepcionNegocio("Debe enviar al menos un jurado para sincronizar");
		}
		return solicitudes.stream()
			.map(this::guardar)
			.map(this::mapear)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<JuradoMesaRespuestaDto> listarJurados() {
		return juradoMesaRepositorio.findAll().stream()
			.sorted(Comparator.comparing(JuradoMesa::getId))
			.map(this::mapear)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<Long> listarMesas() {
		return juradoMesaRepositorio.findAll().stream()
			.map(JuradoMesa::getMesaId)
			.distinct()
			.sorted()
			.toList();
	}

	@Transactional(readOnly = true)
	public List<JuradoMesaRespuestaDto> listarJuradosPorMesa(Long mesaId) {
		return juradoMesaRepositorio.findByMesaId(mesaId).stream()
			.sorted(Comparator.comparing(JuradoMesa::getId))
			.map(this::mapear)
			.toList();
	}

	@Transactional(readOnly = true)
	public AperturaMesaRespuestaDto abrirMesa(Long mesaId, AperturaMesaSolicitudDto solicitud) {
		if (solicitud == null || solicitud.getTokens() == null) {
			throw new ExcepcionAccesoDenegado("Se requieren al menos 2 tokens para abrir la mesa");
		}
		Set<String> tokensUnicos = new HashSet<>(solicitud.getTokens().stream().filter(token -> token != null && !token.isBlank()).toList());
		if (tokensUnicos.size() < 2) {
			throw new ExcepcionAccesoDenegado("Se requieren al menos 2 tokens válidos para abrir la mesa");
		}
		List<JuradoMesa> juradosValidos = juradoMesaRepositorio.encontrarPorMesaIdYTokens(mesaId, tokensUnicos);
		if (juradosValidos.size() != tokensUnicos.size()) {
			throw new ExcepcionAccesoDenegado("Los tokens no corresponden a jurados asignados a esta mesa");
		}
		return new AperturaMesaRespuestaDto("ABIERTA", List.of("E-11", "E-9"));
	}

	private JuradoMesa guardar(SincronizarJuradoMesaSolicitudDto solicitud) {
		if (solicitud.getMesaId() == null) {
			throw new ExcepcionNegocio("Cada jurado debe incluir mesaId");
		}
		if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
			throw new ExcepcionNegocio("El nombre del jurado es obligatorio");
		}
		if (solicitud.getDocumento() == null || solicitud.getDocumento().isBlank()) {
			throw new ExcepcionNegocio("El documento del jurado es obligatorio");
		}
		if (solicitud.getRol() == null || solicitud.getRol().isBlank()) {
			throw new ExcepcionNegocio("El rol del jurado es obligatorio");
		}
		JuradoMesa jurado = new JuradoMesa(solicitud.getNombre(), solicitud.getDocumento(), solicitud.getMesaId(),
				solicitud.getRol(), UUID.randomUUID().toString());
		return juradoMesaRepositorio.save(jurado);
	}

	private JuradoMesaRespuestaDto mapear(JuradoMesa jurado) {
		return new JuradoMesaRespuestaDto(jurado.getId(), jurado.getNombre(), jurado.getDocumento(), jurado.getMesaId(),
				jurado.getRol(), jurado.getTokenAcceso());
	}
}