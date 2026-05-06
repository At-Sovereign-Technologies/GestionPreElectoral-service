package com.selloLegitimo.GestionPreElectoral.servicio.regla;

import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;

@Component
public class ReglaDocumentoNoVotable implements ReglaInhabilidad {

	@Override
	public String getCodigo() {
		return "INH-002";
	}

	@Override
	public ViolacionInhabilidadDto aplicar(Candidatura candidatura, ContextoValidacionDto contexto) {
		if (contexto.getEleccion() == null || contexto.getEleccion().getDocumentoNoVotable() == null) {
			return null;
		}
		String noVotable = contexto.getEleccion().getDocumentoNoVotable();
		if (!"N/A".equalsIgnoreCase(noVotable) && candidatura.getDocumento().equalsIgnoreCase(noVotable)) {
			return new ViolacionInhabilidadDto(
				getCodigo(),
				"El documento del candidato coincide con el documento no votable configurado para la elección",
				ViolacionInhabilidadDto.Severidad.BLOQUEANTE
			);
		}
		return null;
	}
}
