package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricasListaBlancaDto {

    private long totalCiudadanos;

    private List<ZonaConteo> porZona;

    private List<PeriodoConteo> porPeriodo;

    private List<EstadoConteo> porEstado;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZonaConteo {

        private String zona;

        private long conteo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodoConteo {

        private String periodo;

        private long conteo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoConteo {

        private String estado;

        private long conteo;
    }
}