package com.sodimac.rebates.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sodimac.rebates.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, Integer> {

	List<Documento> findByActivo(boolean activo);

	List<Documento> findByRequeridoAndActivo(boolean requerido, boolean activo);

	List<Documento> findByActivoAndIdDocumentoIn(boolean activo, Collection<Integer> idDocumento);

	@Query("SELECT d FROM Documento d WHERE d.idDocumento IN(8, 13) AND d.activo = true AND NOT EXISTS(SELECT c FROM ControlDocumento c WHERE d.idDocumento = c.documento.idDocumento AND c.activo = true AND c.periodo.idCatPeriodo = :#{#idCatPeriodo})")
	List<Documento> findByActivoAndRequiredContabilizar(@Param("idCatPeriodo") Integer idCatPeriodo);

}
