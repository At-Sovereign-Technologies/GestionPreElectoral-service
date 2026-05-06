package com.selloLegitimo.GestionPreElectoral.servicio;

import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;

public interface PuertoAuditoria {
	void registrarEvento(EventoAuditoria evento);
}
