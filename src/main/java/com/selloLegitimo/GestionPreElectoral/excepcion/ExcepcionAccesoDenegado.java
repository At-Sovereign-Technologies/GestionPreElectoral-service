package com.selloLegitimo.GestionPreElectoral.excepcion;

public class ExcepcionAccesoDenegado extends RuntimeException {

	public ExcepcionAccesoDenegado(String mensaje) {
		super(mensaje);
	}
}