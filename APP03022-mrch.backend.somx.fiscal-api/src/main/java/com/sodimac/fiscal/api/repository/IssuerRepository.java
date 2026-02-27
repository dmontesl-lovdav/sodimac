package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssuerRepository extends JpaRepository<IssuerEntity, UUID> {

    /**
     * Busca un emisor por su RFC.
     *
     * @param rfc RFC del emisor
     * @return Optional con el emisor si existe
     */
    Optional<IssuerEntity> findByRfc(String rfc);

}