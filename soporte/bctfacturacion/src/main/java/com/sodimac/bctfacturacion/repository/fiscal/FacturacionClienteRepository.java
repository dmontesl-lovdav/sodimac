package com.sodimac.bctfacturacion.repository.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.fiscal.FacturacionClienteEntity;

@Repository
public interface FacturacionClienteRepository extends JpaRepository<FacturacionClienteEntity, String>  {

	@Modifying
	@Query(value = "{call uspRegistraFacturacionCliente(:pTicket, :pFechaTimbrado, :pUuid, :pTransaccion, :pSerie, :pFolio, :pTienda, :pFechaCarga, :pSubtotal, :pTotal, :pFechaTicket)}", nativeQuery = true)
	public void registraFacturacionCliente(@Param("pTicket") String pTicket
								  , @Param("pFechaTimbrado") String pFechaTimbrado
								  , @Param("pUuid") String pUuid
								  , @Param("pTransaccion") String pTransaccion
								  , @Param("pSerie") String pSerie
								  , @Param("pFolio") Integer pFolio
								  , @Param("pTienda") String pTienda
								  , @Param("pFechaCarga") String pFechaCarga
								  , @Param("pSubtotal") Double pSubtotal
								  , @Param("pTotal") Double pTotal
								  , @Param("pFechaTicket") String pFechaTicket);

	@Query(value = "SELECT COUNT(1) "
			+ " FROM FACTURACION_CLIENTE "
			+ " WHERE UUID = :pUuid ", nativeQuery = true)
	public Integer existeTicket(@Param("pUuid") String pUuid);
	
}
