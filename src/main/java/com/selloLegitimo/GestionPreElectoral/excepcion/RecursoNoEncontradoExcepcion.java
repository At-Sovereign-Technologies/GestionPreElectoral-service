package com.selloLegitimo.GestionPreElectoral.excepcion;

public class RecursoNoEncontradoExcepcion extends RuntimeException {

	public RecursoNoEncontradoExcepcion(String mensaje) {
		super(mensaje);
	}
}