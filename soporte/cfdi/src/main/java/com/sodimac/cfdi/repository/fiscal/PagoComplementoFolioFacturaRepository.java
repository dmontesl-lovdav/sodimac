package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.cfdi.entity.fiscal.PagoComplementoFolioFacturaEntity;

public interface PagoComplementoFolioFacturaRepository extends JpaRepository<PagoComplementoFolioFacturaEntity, Integer> {

	@Query(value = "{call uspObtenerPagoComplementoFolioFacturaByIdPagoComplemento(:pIdPagoComplemento)}", nativeQuery = true)	
	public Integer getIdPagoComplementoFolioFacturaByIdPago(@Param("pIdPagoComplemento") Integer pIdPagoComplemento);
	
	@Query(value = "{call uspObtenerIdFolioFacturaByIdPagoComplemento(:pIdPagoComplemento)}", nativeQuery = true)	
	public Integer getIdFolioFacturaByIdPagoComplemento(@Param("pIdPagoComplemento") Integer pIdPagoComplemento);
	
	@Modifying
	@Query(value = "{call uspObtenerPagoComplementoByIdPagoComplementoFolioFactura(:pIdPagoComplementoFolioFactura)}", nativeQuery = true)	
	public List<Object[]> getPagoComplementoByIdPagoComplementoFolioFactura(@Param("pIdPagoComplementoFolioFactura") Integer pIdPagoComplementoFolioFactura);
	
	@Modifying
	@Query(value = "{call uspObtenerPagoComplementoByIdFolioFactura(:pIdFolioFactura)}", nativeQuery = true)	
	public List<Object[]> getPagoComplementoByIdFolioFactura(@Param("pIdFolioFactura") Integer pIdFolioFactura);
	
	
	@Modifying
	@Query(value = "{call uspInsertPagoComplementoFolioFactura(:pIdPagoComplemento "
			+ "      , :pIdFolioFactura"
			+ "      , :pTotalFolioFactura"
			+ "      , :pMontoPago)}", nativeQuery = true)
	public void registrarPagoComplementoFolioFactura(@Param("pIdPagoComplemento") Integer pIdPagoComplemento
											, @Param("pIdFolioFactura") Integer pIdFolioFactura
											, @Param("pTotalFolioFactura") Double pTotalFolioFactura
											, @Param("pMontoPago") Double pMontoPago
											);
	
	@Modifying
	@Query(value = "{call uspActualizaSaldoPagoComplemento(:pIdPagoComplemento, :pTotalFacturas, :pSaldoPendiente, :pRfc)}", nativeQuery = true)
	public void actualizarSaldosPagoComplemento(@Param("pIdPagoComplemento") Integer idPagoComplemento
								, @Param("pTotalFacturas") Double totalFacturas
								 , @Param("pSaldoPendiente") Double saldoPendiente
								 , @Param("pRfc") String rfc);
	
	
	@Modifying
	@Query(value = "{call uspDesvincularPagoCompFolioFactura(:pIdPagoComplemento)}", nativeQuery = true)
	public void desvincularPagoComplemento(@Param("pIdPagoComplemento") Integer idPagoComplemento);
}
