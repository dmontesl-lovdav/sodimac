package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.ModuleResolver;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleResolverRepository
        extends JpaRepository<ModuleResolver, Integer> {

    List<ModuleResolver> findByResolverEmailIgnoreCase(String resolverEmail);

    Page<ModuleResolver> findByModuleId(Integer moduleId, Pageable pageable);
}
