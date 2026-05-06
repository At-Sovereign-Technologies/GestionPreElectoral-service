package com.selloLegitimo.GestionPreElectoral.servicio.regla;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.repositorio.CiudadanoRepositorio;

@Component
public class ReglaEdadMinima implements ReglaInhabilidad {

	@Autowired
	private CiudadanoRepositorio ciudadanoRepositorio;

	@Override
	public String getCodigo() {
		return "INH-003";
	}

	@Override
	public ViolacionInhabilidadDto aplicar(Candidatura candidatura, ContextoValidacionDto contexto) {
		var ciudadanoOpt = ciudadanoRepositorio.findByNumeroDocumento(candidatura.getDocumento());
		if (ciudadanoOpt.isEmpty()) {
			return new ViolacionInhabilidadDto(
					getCodigo(),
					"Ciudadano no encontrado en el censo",
					ViolacionInhabilidadDto.Severidad.BLOQUEANTE);
		}

		Ciudadano ciudadano = ciudadanoOpt.get();
		LocalDate fechaNacimiento = ciudadano.getFechaNacimiento();
		if (fechaNacimiento == null) {
			return new ViolacionInhabilidadDto(
					getCodigo(),
					"Fecha de nacimiento no registrada para el ciudadano",
					ViolacionInhabilidadDto.Severidad.BLOQUEANTE);
		}

		LocalDate fechaValidacion = contexto.getFechaValidacion().toLocalDate();
		int edad = Period.between(fechaNacimiento, fechaValidacion).getYears();

		Integer edadMinima = contexto.getEleccion().getEdadMinimaCandidatura();
		if (edadMinima == null) {
			edadMinima = 18;
		}

		if (edad < edadMinima) {
			return new ViolacionInhabilidadDto(
					getCodigo(),
					"El candidato no cumple la edad mínima requerida (" + edadMinima + " años). Edad actual: " + edad,
					ViolacionInhabilidadDto.Severidad.BLOQUEANTE);
		}

		return null;
	}
}
