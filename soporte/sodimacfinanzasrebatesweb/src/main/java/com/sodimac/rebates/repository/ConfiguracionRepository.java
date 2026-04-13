package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.rebates.model.Configuracion;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {

	List<Configuracion> findByActivo(boolean activo);

	Configuracion findByNombreVariable(String nombreVariable);

}
