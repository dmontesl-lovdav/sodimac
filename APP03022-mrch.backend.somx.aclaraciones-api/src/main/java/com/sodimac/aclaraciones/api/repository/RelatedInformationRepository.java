package com.sodimac.aclaraciones.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.aclaraciones.api.model.entity.RelatedInformation;

public interface RelatedInformationRepository extends JpaRepository<RelatedInformation, Long> {

    List<RelatedInformation> findByBusinessUnitId(Integer businessUnitId);

    List<RelatedInformation> findByCountryId(Integer countryId);

    List<RelatedInformation> findByBusinessUnitIdAndCountryId(Integer businessUnitId, Integer countryId);
}
