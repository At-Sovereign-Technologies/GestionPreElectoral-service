package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class FirmasModificacionDto {

    @NotNull(message = "la firma del Superadministrador es obligatoria")
    @Valid
    private FirmaActorDto superadmin;

    @NotNull(message = "la firma del Delegado CNE es obligatoria")
    @Valid
    private FirmaActorDto cne;

    public FirmasModificacionDto() {
    }

    public FirmaActorDto getSuperadmin() {
        return superadmin;
    }

    public void setSuperadmin(FirmaActorDto superadmin) {
        this.superadmin = superadmin;
    }

    public FirmaActorDto getCne() {
        return cne;
    }

    public void setCne(FirmaActorDto cne) {
        this.cne = cne;
    }
}
