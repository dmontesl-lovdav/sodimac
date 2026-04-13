package com.sodimac.rebates.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.rebates.model.Catalogo;

public interface CatCatalogoRepository extends JpaRepository<Catalogo, Integer>{
	List<Catalogo> findByLogicActive(boolean logicAtive);
	Optional<Catalogo> findByIdCatalogoAndLogicActive(Integer idCatalogo, boolean logicAtive);
	Optional<Catalogo> findByNombreAndLogicActive(String nombre, boolean logicAtive);
	List<Catalogo> findByNombreContainingIgnoreCaseAndLogicActive(String nombre, boolean logicAtive);
	List<Catalogo> findByDescripcionContainingIgnoreCaseAndLogicActive(String descripcion, boolean logicAtive);
}
