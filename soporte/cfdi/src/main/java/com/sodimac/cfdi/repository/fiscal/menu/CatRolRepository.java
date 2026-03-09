package com.sodimac.cfdi.repository.fiscal.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.menu.CatRolEntity;

@Repository
public interface CatRolRepository extends JpaRepository<CatRolEntity, Integer> {

	public List<CatRolEntity> findByIdIn(List<Integer> ids);
}
