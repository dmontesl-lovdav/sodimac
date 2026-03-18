package com.sodimac.facturacion.repository.bct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.bct.TicketBctEntity;

@Repository
public interface TicketBctRepository extends JpaRepository<TicketBctEntity, String> {

    @Query(value = "select NUM_TRX, FECHA_TRX, NUM_TIENDA, NUM_CAJA, COD_CAJERO, NUM_TICKET, TIPO_TRX, MNT_TOTAL_A_PAGAR, MNT_TOT_SN_IMPTOS, TRX_ORIGINAL, CASE WHEN TRX_ORIGINAL IS NULL THEN 'N' ELSE 'Y' END AS ID_CLIENTE from TRX_HDR where NUM_TRX = :ticket and TIPO_TRX in (1, 9, 10) and rownum <= 1", nativeQuery = true) 
    Object findByTicket(@Param("ticket") String ticket);
	
}
