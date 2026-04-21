package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CausalesEleccionDto {

    private List<CausalItemDto> excluido;
    private List<CausalItemDto> exento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CausalItemDto {
        private String valor;
        private String etiqueta;
    }
}
