package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatCompradorProveedorEntity;

@Repository
public interface CatCompradorProveedorRepository extends JpaRepository<CatCompradorProveedorEntity, Integer> {

	public CatCompradorProveedorEntity findByIdcompradorAndNumeroProveedor(Integer idcomprador, String numeroProveedor);

}
