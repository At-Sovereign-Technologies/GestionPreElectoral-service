package com.selloLegitimo.GestionPreElectoral.servicio.regla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.CausalCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.modelo.RegistroCenso;
import com.selloLegitimo.GestionPreElectoral.repositorio.CiudadanoRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.RegistroCensoRepositorio;

@Component
public class ReglaCondenaJudicial implements ReglaInhabilidad {

	@Autowired
	private CiudadanoRepositorio ciudadanoRepositorio;

	@Autowired
	private RegistroCensoRepositorio registroCensoRepositorio;

	@Override
	public String getCodigo() {
		return "INH-004";
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
		var registroOpt = registroCensoRepositorio.findByEleccionIdAndCiudadano(
				candidatura.getEleccionId(), ciudadano);

		if (registroOpt.isEmpty()) {
			return new ViolacionInhabilidadDto(
					getCodigo(),
					"El ciudadano no está inscrito en el censo de esta elección",
					ViolacionInhabilidadDto.Severidad.BLOQUEANTE);
		}

		RegistroCenso registro = registroOpt.get();
		if (registro.getCausalEstado() == CausalCenso.CONDENA_CON_PENA_ACCESORIA) {
			return new ViolacionInhabilidadDto(
					getCodigo(),
					"El ciudadano tiene condena judicial con pena accesoria registrada en el censo",
					ViolacionInhabilidadDto.Severidad.BLOQUEANTE);
		}

		return null;
	}
}
