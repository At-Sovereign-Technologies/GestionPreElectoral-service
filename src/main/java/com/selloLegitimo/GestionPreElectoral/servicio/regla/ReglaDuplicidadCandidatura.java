package com.selloLegitimo.GestionPreElectoral.servicio.regla;

import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;

@Component
public class ReglaDuplicidadCandidatura implements ReglaInhabilidad {

	@Override
	public String getCodigo() {
		return "INH-001";
	}

	@Override
	public ViolacionInhabilidadDto aplicar(Candidatura candidatura, ContextoValidacionDto contexto) {
		return null;
	}
}
