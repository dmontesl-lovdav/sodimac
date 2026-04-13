package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sodimac.rebates.model.ProgramaPago;

public interface ProgramaPagoRepository extends JpaRepository<ProgramaPago, Integer> {

	List<ProgramaPago> findByActivo(boolean activo);

}
