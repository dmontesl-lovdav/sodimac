package com.sodimac.cfdi.repository.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatMensajesEntity;

@Repository("catMensajesRepository")
public interface CatMensajesRepository extends JpaRepository<CatMensajesEntity, Integer> {

}
