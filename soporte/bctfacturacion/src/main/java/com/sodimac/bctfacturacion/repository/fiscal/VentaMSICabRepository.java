package com.sodimac.bctfacturacion.repository.fiscal;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.fiscal.VentaMSICabEntity;

@Repository
public interface VentaMSICabRepository extends JpaRepository<VentaMSICabEntity, String> {

	
	@Query(value = "SELECT COUNT(1) "
			+ "FROM VENTA_CAB "
			+ "WHERE FECHA_TICKET = :pFechaTicket "
			+ "AND   TIENDA = :pTienda", nativeQuery = true)
	public Integer totalTickets(@Param("pFechaTicket") String fechaTicket
			  				  , @Param("pTienda") Integer tienda);
	
	@Query(value = "SELECT COUNT(1) "
			+ "FROM VENTA_DET "
			+ "WHERE FECHA_TICKET = :pFechaTicket "
			+ "AND   TIENDA = :pTienda", nativeQuery = true)
	public Integer totalTicketsDet(@Param("pFechaTicket") String fechaTicket
			  				  , @Param("pTienda") Integer tienda);

	@Modifying
	@Query(value = "{call uspEliminaVentaCab(:pFechaTicket, :pTienda)}", nativeQuery = true)
	public void eliminaVentaCab(@Param("pFechaTicket") Date fechaTicket
			  				  , @Param("pTienda") Integer tienda);
	
	@Modifying
	@Query(value = "{call uspEliminaVentaDet(:pFechaTicket, :pTienda)}", nativeQuery = true)
	public void eliminaVentaDet(@Param("pFechaTicket") Date fechaTicket
			  				  , @Param("pTienda") Integer tienda);
	
	@Modifying
	@Query(value = "{call uspRegistraVentaCab(:pTicket, :pFechaTicket, :pTienda, :pCaja, :pTransaccion, :pTipo, :pTotal, :pSubtotal, :pRedondeo, :pTicketOrigen, :pFechaEnlace, :pFechaCarga, :pEstatusProceso)}", nativeQuery = true)
	public void registraVentaCab(@Param("pTicket") String ticket
								  , @Param("pFechaTicket") Date fechaTicket
								  , @Param("pTienda") Integer tienda
								  , @Param("pCaja") Integer caja
								  , @Param("pTransaccion") Integer transaccion
								  , @Param("pTipo") Integer tipo
								  , @Param("pTotal") Double total
								  , @Param("pSubtotal") Double subtotal
								  , @Param("pRedondeo") Double redondeo
								  , @Param("pTicketOrigen") String ticketOrigen
								  , @Param("pFechaEnlace") Date fechaEnlace
								  , @Param("pFechaCarga") Date fechaCarga
								  , @Param("pEstatusProceso") Integer estatusProceso);
	
	@Modifying
	@Query(value = "{call uspRegistraVentaCabDet(:pTicket, :pFechaTicket, :pTienda, :pCaja, :pNumDocCanal, :pCanalLinio, :pTotalArticulo, :pFechaCarga, :pCajaEstatusProceso)}", nativeQuery = true)
	public void registraVentaCabDet(@Param("pTicket") String ticket
								  , @Param("pFechaTicket") Date fechaTicket
								  , @Param("pTienda") Integer tienda
								  , @Param("pCaja") Integer caja
								  , @Param("pNumDocCanal") String numDocCanal
								  , @Param("pCanalLinio") String canalLinio
								  , @Param("pTotalArticulo") Integer totalArticulo
								  , @Param("pFechaCarga") Date fechaCarga
								  , @Param("pCajaEstatusProceso") Integer cajaEstatusProceso);
	
}
