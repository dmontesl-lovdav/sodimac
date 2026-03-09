package com.sodimac.cfdi.repository.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.ComplementosEntity;

@Repository("complementosRepository")
public interface ComplementosRepository extends JpaRepository<ComplementosEntity, Integer> {

	public ComplementosEntity findByTicket(String ticket);	

}
