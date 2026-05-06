package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ciudadanos", uniqueConstraints = {
	@UniqueConstraint(name = "uk_ciudadano_documento", columnNames = { "tipo_documento", "numero_documento" })
})
public class Ciudadano {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_documento", nullable = false, length = 20)
	private String tipoDocumento;

	@Column(name = "numero_documento", nullable = false, length = 30)
	private String numeroDocumento;

	@Column(nullable = false, length = 120)
	private String nombres;

	@Column(nullable = false, length = 120)
	private String apellidos;

	@Column(name = "fecha_nacimiento")
	private LocalDate fechaNacimiento;

	@Column(name = "departamento", length = 80)
	private String departamento;

	@Column(name = "municipio", length = 120)
	private String municipio;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private LocalDateTime fechaActualizacion;

	public Ciudadano() {
	}

	public Ciudadano(String tipoDocumento, String numeroDocumento, String nombres, String apellidos, LocalDate fechaNacimiento) {
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.fechaNacimiento = fechaNacimiento;
	}

	public Ciudadano(String tipoDocumento, String numeroDocumento, String nombres, String apellidos, LocalDate fechaNacimiento, String departamento, String municipio) {
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.fechaNacimiento = fechaNacimiento;
		this.departamento = departamento;
		this.municipio = municipio;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime ahora = LocalDateTime.now();
		this.fechaCreacion = ahora;
		this.fechaActualizacion = ahora;
	}

	@PreUpdate
	public void preUpdate() {
		this.fechaActualizacion = LocalDateTime.now();
	}

	public void actualizarDatos(String nombres, String apellidos, LocalDate fechaNacimiento) {
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.fechaNacimiento = fechaNacimiento;
	}

	public void actualizarDatosGeo(String departamento, String municipio) {
		this.departamento = departamento;
		this.municipio = municipio;
	}

	public Long getId() {
		return id;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public String getNombres() {
		return nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public String getDepartamento() {
		return departamento;
	}

	public String getMunicipio() {
		return municipio;
	}
}