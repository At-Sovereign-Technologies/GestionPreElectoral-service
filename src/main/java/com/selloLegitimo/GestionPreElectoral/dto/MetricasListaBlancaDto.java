package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

public class MetricasListaBlancaDto {

    private long totalCiudadanos;
    private List<ZonaConteo> porZona;

    public MetricasListaBlancaDto() {}

    public MetricasListaBlancaDto(long totalCiudadanos, List<ZonaConteo> porZona) {
        this.totalCiudadanos = totalCiudadanos;
        this.porZona = porZona;
    }

    public long getTotalCiudadanos() {
        return totalCiudadanos;
    }

    public void setTotalCiudadanos(long totalCiudadanos) {
        this.totalCiudadanos = totalCiudadanos;
    }

    public List<ZonaConteo> getPorZona() {
        return porZona;
    }

    public void setPorZona(List<ZonaConteo> porZona) {
        this.porZona = porZona;
    }

    public static class ZonaConteo {
        private String zona;
        private long conteo;

        public ZonaConteo() {}

        public ZonaConteo(String zona, long conteo) {
            this.zona = zona;
            this.conteo = conteo;
        }

        public String getZona() {
            return zona;
        }

        public void setZona(String zona) {
            this.zona = zona;
        }

        public long getConteo() {
            return conteo;
        }

        public void setConteo(long conteo) {
            this.conteo = conteo;
        }
    }
}
