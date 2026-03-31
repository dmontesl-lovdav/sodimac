package com.sodimac.bctfacturacion.repository.ces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.ces.ControVentaCesEntity;

@Repository
public interface ControVentaCesRepository extends JpaRepository<ControVentaCesEntity, Integer> {

	@Query(value = "SELECT NEXT VALUE FOR SQ_CONTROL_CES", nativeQuery = true)
	Integer getIdControlCes();

	@Query(value = "{call uspObtieneControlCesPorFecha(:pFecha)}", nativeQuery = true)
	Integer getIdControlCesByFecha(@Param("pFecha") String paramString);

	@Query(value = "{call uspObtieneEstatusControlCesPorFecha(:pIdControlVentaCes)}", nativeQuery = true)
	Integer getEstatusControlCesByFecha(@Param("pIdControlVentaCes") Integer paramInteger);

	@Query(value = "{call uspExisteControlCes(:pFecha)}", nativeQuery = true)
	Object[] existeControlCes(@Param("pFecha") String paramString);

	@Modifying
	@Query(value = "{call uspRegistraControlCes(:pIdControlVentaCes, :pFecha, :pTotalVentasCes)}", nativeQuery = true)
	void registraControlCes(@Param("pIdControlVentaCes") Integer paramInteger1, @Param("pFecha") String paramString,
			@Param("pTotalVentasCes") Integer paramInteger2);

	@Modifying
	@Query(value = "{call uspActualizaControlCes(:pIdControlVentaCes, :pEstatus, :pTotalRegistrados)}", nativeQuery = true)
	void actualizaControlCes(@Param("pIdControlVentaCes") Integer paramInteger1,
			@Param("pEstatus") Integer paramInteger2, @Param("pTotalRegistrados") Integer paramInteger3);

	@Query(value = "{call uspObtieneTransCesPermitidos()}", nativeQuery = true)
	String[] getTipoTransCesPermitidos();
}
