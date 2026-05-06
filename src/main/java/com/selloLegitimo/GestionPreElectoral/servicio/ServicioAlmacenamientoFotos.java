package com.selloLegitimo.GestionPreElectoral.servicio;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;

import jakarta.annotation.PostConstruct;

@Service
public class ServicioAlmacenamientoFotos {

	private static final Logger logger = LoggerFactory.getLogger(ServicioAlmacenamientoFotos.class);

	private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png");
	private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png");
	private static final long TAMANIO_MAXIMO = 2 * 1024 * 1024;
	private static final int RESOLUCION_MINIMA = 200;

	@Value("${fotos.directorio:/data/fotos-candidatos}")
	private String directorioFotos;

	private Path rutaBase;

	@PostConstruct
	public void inicializar() {
		rutaBase = Paths.get(directorioFotos);
		try {
			Files.createDirectories(rutaBase);
			logger.info("Directorio de fotos inicializado: {}", rutaBase.toAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("No se pudo crear el directorio de almacenamiento de fotos: " + directorioFotos, e);
		}
	}

	public String almacenarFoto(Long candidaturaId, MultipartFile archivo) {
		validarTipoContenido(archivo);
		validarTamanio(archivo);
		String extension = obtenerExtension(archivo);
		validarDimensiones(archivo);

		String nombreArchivo = candidaturaId + "_" + Instant.now().toEpochMilli() + "." + extension;
		Path rutaDestino = rutaBase.resolve(nombreArchivo);

		try (InputStream entrada = archivo.getInputStream()) {
			Files.copy(entrada, rutaDestino, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Error al almacenar la foto del candidato", e);
		}

		logger.info("Foto almacenada para candidatura {}: {}", candidaturaId, nombreArchivo);
		return nombreArchivo;
	}

	public Resource cargarFoto(String nombreArchivo) {
		Path archivo = rutaBase.resolve(nombreArchivo).normalize();
		if (!archivo.startsWith(rutaBase)) {
			throw new ExcepcionNegocio("Ruta de archivo no permitida");
		}
		if (!Files.exists(archivo) || !Files.isReadable(archivo)) {
			throw new RecursoNoEncontradoExcepcion("Foto no encontrada: " + nombreArchivo);
		}
		try {
			return new UrlResource(archivo.toUri());
		} catch (IOException e) {
			throw new RecursoNoEncontradoExcepcion("Foto no encontrada: " + nombreArchivo);
		}
	}

	public String resolverRutaFoto(Long candidaturaId) {
		try {
			return Files.list(rutaBase)
					.filter(p -> p.getFileName().toString().startsWith(candidaturaId + "_"))
					.sorted((a, b) -> {
						long tsA = extraerTimestamp(a.getFileName().toString());
						long tsB = extraerTimestamp(b.getFileName().toString());
						return Long.compare(tsB, tsA);
					})
					.findFirst()
					.map(p -> p.getFileName().toString())
					.orElse(null);
		} catch (IOException e) {
			return null;
		}
	}

	public String determinarTipoContenido(String nombreArchivo) {
		if (nombreArchivo == null) {
			return "application/octet-stream";
		}
		String nombreLower = nombreArchivo.toLowerCase();
		if (nombreLower.endsWith(".jpg") || nombreLower.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (nombreLower.endsWith(".png")) {
			return "image/png";
		}
		return "application/octet-stream";
	}

	private long extraerTimestamp(String nombreArchivo) {
		try {
			String sinExtension = nombreArchivo.substring(nombreArchivo.indexOf('_') + 1, nombreArchivo.lastIndexOf('.'));
			return Long.parseLong(sinExtension);
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	private void validarTipoContenido(MultipartFile archivo) {
		String tipoContenido = archivo.getContentType();
		if (tipoContenido == null || !TIPOS_PERMITIDOS.contains(tipoContenido.toLowerCase())) {
			throw new ExcepcionNegocio("Solo se permiten archivos JPG y PNG");
		}
	}

	private void validarTamanio(MultipartFile archivo) {
		if (archivo.getSize() > TAMANIO_MAXIMO) {
			throw new ExcepcionNegocio("El archivo excede el tamaño máximo permitido de 2MB");
		}
		if (archivo.isEmpty()) {
			throw new ExcepcionNegocio("El archivo está vacío");
		}
	}

	private String obtenerExtension(MultipartFile archivo) {
		String nombreOriginal = archivo.getOriginalFilename();
		if (nombreOriginal == null || !nombreOriginal.contains(".")) {
			throw new ExcepcionNegocio("El archivo no tiene una extensión válida");
		}
		String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
		if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
			throw new ExcepcionNegocio("Solo se permiten archivos con extensión JPG y PNG");
		}
		return extension.equals("jpeg") ? "jpg" : extension;
	}

	private void validarDimensiones(MultipartFile archivo) {
		try (InputStream entrada = archivo.getInputStream()) {
			BufferedImage imagen = ImageIO.read(entrada);
			if (imagen == null) {
				throw new ExcepcionNegocio("No se pudo leer la imagen. Asegúrese de que el archivo sea una imagen válida");
			}
			if (imagen.getWidth() < RESOLUCION_MINIMA || imagen.getHeight() < RESOLUCION_MINIMA) {
				throw new ExcepcionNegocio("La resolución mínima de la imagen es " + RESOLUCION_MINIMA + "x" + RESOLUCION_MINIMA + " píxeles");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error al leer la imagen para validación de dimensiones", e);
		}
	}
}