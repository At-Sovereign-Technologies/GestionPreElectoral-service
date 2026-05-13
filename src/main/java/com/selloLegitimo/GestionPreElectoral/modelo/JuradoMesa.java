package com.selloLegitimo.GestionPreElectoral.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jurados_mesa", schema = "gestion_pre_electoral", uniqueConstraints = {
	@UniqueConstraint(name = "uk_jurado_mesa_documento_mesa", columnNames = { "mesa_id", "documento" }),
	@UniqueConstraint(name = "uk_jurado_mesa_token", columnNames = { "token_acceso" })
})
public class JuradoMesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 180)
	private String nombre;

	@Column(nullable = false, length = 30)
	private String documento;

	@Column(name = "mesa_id", nullable = false)
	private Long mesaId;

	@Column(nullable = false, length = 50)
	private String rol;

	@Column(name = "token_acceso", nullable = false, length = 36)
	private String tokenAcceso;

	public JuradoMesa() {
	}

	public JuradoMesa(String nombre, String documento, Long mesaId, String rol, String tokenAcceso) {
		this.nombre = nombre;
		this.documento = documento;
		this.mesaId = mesaId;
		this.rol = rol;
		this.tokenAcceso = tokenAcceso;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDocumento() {
		return documento;
	}

	public Long getMesaId() {
		return mesaId;
	}

	public String getRol() {
		return rol;
	}

	public String getTokenAcceso() {
		return tokenAcceso;
	}
}