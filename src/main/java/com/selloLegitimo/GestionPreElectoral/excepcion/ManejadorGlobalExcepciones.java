package com.selloLegitimo.GestionPreElectoral.excepcion;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.selloLegitimo.GestionPreElectoral.dto.ErrorRespuestaDto;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

	@ExceptionHandler(RecursoNoEncontradoExcepcion.class)
	public ResponseEntity<ErrorRespuestaDto> manejarNoEncontrado(RecursoNoEncontradoExcepcion ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorRespuestaDto(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage()));
	}

	@ExceptionHandler({ ExcepcionNegocio.class, IllegalArgumentException.class })
	public ResponseEntity<ErrorRespuestaDto> manejarNegocio(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorRespuestaDto(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorRespuestaDto> manejarValidacion(MethodArgumentNotValidException ex) {
		String mensaje = ex.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(error -> error.getField() + " " + error.getDefaultMessage())
			.orElse("La solicitud no es válida");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorRespuestaDto(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), mensaje));
	}
}