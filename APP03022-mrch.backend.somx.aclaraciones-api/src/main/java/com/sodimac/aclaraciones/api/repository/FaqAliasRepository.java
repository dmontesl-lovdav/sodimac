package com.sodimac.aclaraciones.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sodimac.aclaraciones.api.model.entity.FaqAlias;

public interface FaqAliasRepository extends JpaRepository<FaqAlias, Long> {
}
