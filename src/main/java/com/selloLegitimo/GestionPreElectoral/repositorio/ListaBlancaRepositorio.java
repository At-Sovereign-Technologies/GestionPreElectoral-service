package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlanca;

@Repository
public interface ListaBlancaRepositorio extends JpaRepository<ListaBlanca, UUID> {

    Optional<ListaBlanca> findByCiudadanoId(String ciudadanoId);

    Optional<ListaBlanca> findByNumeroDocumento(String numeroDocumento);

    long countByEstado(String estado);

    @Query("SELECT l.zonaInscripcion, COUNT(l) FROM ListaBlanca l WHERE l.estado = 'HABILITADO' GROUP BY l.zonaInscripcion")
    List<Object[]> contarPorZona();

    @Query("SELECT l FROM ListaBlanca l WHERE l.estado = 'HABILITADO' ORDER BY l.numeroDocumento ASC")
    List<ListaBlanca> listarActivosOrdenados();

    List<ListaBlanca> findByRol(String rol);
}