package com.sodimac.aclaraciones.api.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.sodimac.aclaraciones.api.model.entity.Sla;

public interface SlaRepository extends CrudRepository<Sla, Integer> {

    List<Sla> findByActive(boolean active);

    boolean existsByModule_IdAndActive(int moduleId, boolean active);

    Sla findFirstByModule_IdAndActiveTrueAndStatus(int moduleId, int status);

}
