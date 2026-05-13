package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.HistoricoIntegridadDto;
import com.selloLegitimo.GestionPreElectoral.dto.MetricasListaBlancaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ModificarEmergenciaRequestDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificarIntegridadResponseDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioListaBlanca;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/lista-blanca")
public class ControladorListaBlanca {

    private final ServicioListaBlanca servicio;

    public ControladorListaBlanca(ServicioListaBlanca servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/metricas")
    public ResponseEntity<MetricasListaBlancaDto> obtenerMetricas() {
        MetricasListaBlancaDto dto = servicio.obtenerMetricas();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/verificar")
    public ResponseEntity<VerificarIntegridadResponseDto> verificar() {
        String hash = servicio.verificarIntegridad();
        VerificarIntegridadResponseDto resp = new VerificarIntegridadResponseDto(hash, "FIRMADA");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/modificar-emergencia")
    public ResponseEntity<Void> modificarEmergencia(@Valid @RequestBody ModificarEmergenciaRequestDto req) {
        servicio.modificarEmergencia(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar/historico")
    public ResponseEntity<List<HistoricoIntegridadDto>> obtenerHistorico() {

        List<HistoricoIntegridadDto> historial =
                servicio.obtenerHistoricoIntegridad();

        return ResponseEntity.ok(historial);
    }
}
