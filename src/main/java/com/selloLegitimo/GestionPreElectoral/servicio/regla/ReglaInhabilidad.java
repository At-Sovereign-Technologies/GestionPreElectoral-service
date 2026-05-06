package com.selloLegitimo.GestionPreElectoral.servicio.regla;

import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.ViolacionInhabilidadDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;

public interface ReglaInhabilidad {
	String getCodigo();
	ViolacionInhabilidadDto aplicar(Candidatura candidatura, ContextoValidacionDto contexto);
}
