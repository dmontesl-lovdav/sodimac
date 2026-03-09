package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.cfdi.entity.fiscal.PagoComplementoFolioFacturaDetalleEntity;

public interface PagoComplementoFolioFacturaDetalleRepository extends JpaRepository<PagoComplementoFolioFacturaDetalleEntity, Integer> {

	@Query(value = "{call uspObtenerFacturasByIdPagoComplementoFolioFactura(:pIdPagoComplementoFolioFactura)}", nativeQuery = true)
	public List<Object[]> getFacturasByIdPagoComplementoFolioFactura(@Param("pIdPagoComplementoFolioFactura") Integer pIdPagoComplementoFolioFactura);
	
	@Query(value = "{call uspObtenerFacturasByIdFactura(:pIdFactura)}", nativeQuery = true)
	public List<Object[]> getFacturasByIdFactura(@Param("pIdFactura") Integer pIdFactura);
	
	@Modifying
	@Query(value = "{call uspInsertPagoComplementoFolioFacturaDetalle (:pIdPagoComplementoFolioFactura "
			+ "      , :pIdFactura"
			+ "      , :pParcialidad"
			+ "      , :pEquivalencia"
			+ "      , :pMonedaPago"
			+ "      , :pMontoRealFactura"
			+ "      , :pMontoFactura"
			+ "      , :pMontoTotalNC"
			+ "      , :pImporteSaldoAnterior "
			+ "      , :pImportePagado"
			+ "      , :pImportePosterior"
			+ "      , :pEstatusFactura)}", nativeQuery = true)
	public void registrarPagoComplementoFolioFacturaDetalle(@Param("pIdPagoComplementoFolioFactura") Integer pIdPagoComplementoFolioFactura
											, @Param("pIdFactura") Integer pIdFactura
											, @Param("pParcialidad") Integer pParcialidad
											, @Param("pEquivalencia") Double pEquivalencia
											, @Param("pMonedaPago") String pMonedaPago
											, @Param("pMontoRealFactura") Double pMontoRealFactura
											, @Param("pMontoFactura") Double pMontoFactura
											, @Param("pMontoTotalNC") Double pMontoTotalNC
											, @Param("pImporteSaldoAnterior") Double pImporteSaldoAnterior
											, @Param("pImportePagado") Double pImportePagado
											, @Param("pImportePosterior") Double pImportePosterior
											, @Param("pEstatusFactura") Integer pEstatusFactura
											);
	
}
