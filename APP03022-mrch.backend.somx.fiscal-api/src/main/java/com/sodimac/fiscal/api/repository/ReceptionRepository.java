package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReceptionRepository extends JpaRepository<ReceptionEntity, UUID> {

    /**
     * IDs de recepciones cuya reception_date cae en el rango [start, end] (inclusivo).
     * Usado por la búsqueda de facturas/NC para filtrar por fecha de recepción real
     * (no por la fecha de registro). Ver fix Fer 2026-06-18.
     */
    @Query("SELECT r.receptionId FROM ReceptionEntity r " +
            "WHERE r.receptionDate >= :start AND r.receptionDate <= :end")
    List<UUID> findReceptionIdsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
