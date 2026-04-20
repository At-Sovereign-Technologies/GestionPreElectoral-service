package com.selloLegitimo.GestionPreElectoral.excepcion;

public class ExcepcionNegocio extends RuntimeException {

	public ExcepcionNegocio(String mensaje) {
		super(mensaje);
	}
}