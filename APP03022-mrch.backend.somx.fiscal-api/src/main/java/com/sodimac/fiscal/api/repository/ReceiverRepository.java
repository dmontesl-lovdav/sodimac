package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiverRepository extends JpaRepository<ReceiverEntity, UUID> {

    /**
     * Busca un receptor por su RFC.
     *
     * @param rfc RFC del receptor
     * @return Optional con el receptor si existe
     */
    Optional<ReceiverEntity> findByRfc(String rfc);

}