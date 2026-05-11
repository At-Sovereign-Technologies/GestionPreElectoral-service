package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;
import java.util.UUID;

public class VerificacionActaE14RespuestaDto {

	private boolean valida;
	private UUID actaUuid;
	private int totalVersiones;
	private int versionesVerificadas;
	private List<VersionErrorDto> errores;

	public VerificacionActaE14RespuestaDto() {
	}

	public VerificacionActaE14RespuestaDto(boolean valida, UUID actaUuid, int totalVersiones,
			int versionesVerificadas, List<VersionErrorDto> errores) {
		this.valida = valida;
		this.actaUuid = actaUuid;
		this.totalVersiones = totalVersiones;
		this.versionesVerificadas = versionesVerificadas;
		this.errores = errores;
	}

	public boolean isValida() {
		return valida;
	}

	public UUID getActaUuid() {
		return actaUuid;
	}

	public int getTotalVersiones() {
		return totalVersiones;
	}

	public int getVersionesVerificadas() {
		return versionesVerificadas;
	}

	public List<VersionErrorDto> getErrores() {
		return errores;
	}

	public static class VersionErrorDto {
		private Integer versionNumber;
		private String mensaje;

		public VersionErrorDto() {
		}

		public VersionErrorDto(Integer versionNumber, String mensaje) {
			this.versionNumber = versionNumber;
			this.mensaje = mensaje;
		}

		public Integer getVersionNumber() {
			return versionNumber;
		}

		public String getMensaje() {
			return mensaje;
		}
	}
}
