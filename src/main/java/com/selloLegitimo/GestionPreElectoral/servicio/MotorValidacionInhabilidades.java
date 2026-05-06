package com.selloLegitimo.GestionPreElectoral.servicio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResultadoValidacionInhabilidadesDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.servicio.regla.ReglaInhabilidad;

@Component
public class MotorValidacionInhabilidades {

	private final List<ReglaInhabilidad> reglas;

	@Autowired
	public MotorValidacionInhabilidades(List<ReglaInhabilidad> reglas) {
		this.reglas = reglas;
	}

	public ResultadoValidacionInhabilidadesDto validar(Candidatura candidatura, ContextoValidacionDto contexto) {
		List<ViolacionInhabilidadDto> violaciones = new ArrayList<>();

		for (ReglaInhabilidad regla : reglas) {
			ViolacionInhabilidadDto violacion = regla.aplicar(candidatura, contexto);
			if (violacion != null) {
				violaciones.add(violacion);
			}
		}

		boolean valido = violaciones.stream().noneMatch(v -> v.getSeveridad() == ViolacionInhabilidadDto.Severidad.BLOQUEANTE);

		ResultadoValidacionInhabilidadesDto resultado = new ResultadoValidacionInhabilidadesDto();
		resultado.setCandidaturaId(candidatura.getId());
		resultado.setMensaje(valido ? "Candidatura válida" : "Se encontraron inhabilidades bloqueantes");
		resultado.setValido(valido);
		resultado.setViolaciones(violaciones);
		return resultado;
	}
}
