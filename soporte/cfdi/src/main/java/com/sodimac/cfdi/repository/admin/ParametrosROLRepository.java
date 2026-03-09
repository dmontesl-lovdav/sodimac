package com.sodimac.cfdi.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.admin.UserParamEntity;

@Repository("parametrosRolRepository")
public interface ParametrosROLRepository extends JpaRepository<UserParamEntity, String>{

}
