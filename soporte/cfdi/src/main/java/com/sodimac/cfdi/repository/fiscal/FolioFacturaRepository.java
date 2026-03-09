package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.FolioFacturaEntity;

@Repository
public interface FolioFacturaRepository extends JpaRepository<FolioFacturaEntity, Integer> {
	
	@Query(value = "{call uspObtenerIdFolioFacturaByFolioFactura(:pFolioFactura)}", nativeQuery = true)	
	public Integer getIdFolioFacturaByFolioFactura(@Param("pFolioFactura") Integer pFolioFactura);
	
	@Modifying
	@Query(value = "{call uspObtenerFoliosFacturasPPD (:fechaInicial, :fechaFinal, :pRfc, :pFolio, :start, :rowsPerPage )}", nativeQuery = true)	
	public List<Object[]> getFoliosFacturaPPDByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("pRfc") String rfc
			, @Param("pFolio") Integer pFolio
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage);
	
	@Query(value = "select (max(folioFactura) + 1) from foliofactura", nativeQuery = true) 
    public Integer getMaxFolioFactura();

	@Modifying
	@Query(value = "{call uspRegistrarFoliosFacturasPPD(:idFactura, :folioFactura)}", nativeQuery = true)
	public void registrarFolioFactura(@Param("idFactura") Integer idFactura
									, @Param("folioFactura") Integer folioFactura);
	
	
	@Modifying
	@Query(value = "{call uspRegistrarFoliosFacturasDetPPD(:pIdFolioFactura, :pIdFactura, :pTotal, :pNotaCredito, :pMontoRefactura, :pOrden)}", nativeQuery = true)
	public void registrarFolioFacturaDet(@Param("pIdFolioFactura") Integer pIdFolioFactura
									, @Param("pIdFactura") Integer pIdFactura
									, @Param("pTotal") Double pTotal
									, @Param("pNotaCredito") Integer pNotaCredito
									, @Param("pMontoRefactura") Double pMontoRefactura
									, @Param("pOrden") Integer pOrden);
	
	
	@Modifying
	@Query(value = "{call uspRegistrarFoliosFacturasImpuesto(:folioFactura, :idFactura, :base, :tipoImpuesto, :tasa, :monto )}", nativeQuery = true)
	public void registrarFolioFacturaImpuesto(@Param("folioFactura") Integer folioFactura
											, @Param("idFactura") Integer idFactura
											, @Param("base") String base
											, @Param("tipoImpuesto") String tipoImpuesto
											, @Param("tasa") String tasa
											, @Param("monto") String monto);

	@Modifying
	@Query(value = "{call uspDesvincularFacturaPPD(:idFactura)}", nativeQuery = true)
	public void desvincularFolio(@Param("idFactura") Integer idFactura);

	@Modifying
	@Query(value = "{call uspObtenerPagosFoliosFactura(:pIdPagoComplemento, :pFolioFactura)}", nativeQuery = true)	
	public List<Object[]> getPagosComplementoFoliosFactura( @Param("pIdPagoComplemento") Integer pIdPagoComplemento
														  , @Param("pFolioFactura") Integer pFolioFactura );
	
	@Query(value = "{call uspObtenerSumaFoliosFactura(:pFolioFactura)}", nativeQuery = true)
	public Double getSumaFoliosFactura(Integer pFolioFactura);
	
	@Query(value = "{call uspObtenerSumaComplementoFoliosFactura(:pFolioFactura)}", nativeQuery = true)	
	public Double getSumaComplementoFoliosFactura( @Param("pFolioFactura") Integer pFolioFactura );
	
	@Query(value = "{call uspObtenerMontoUuIdRelacionado (:pUuidRelacionado )}", nativeQuery = true)	
	public List<Object[]> getMontoUuiRelacionado( @Param("pUuidRelacionado") String pUuidRelacionado);
	
}
