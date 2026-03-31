package com.sodimac.bctfacturacion.repository.bct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.bct.ViewCinfoTrjBnc;

@Repository
public interface DevolucionesBctRepository extends JpaRepository<ViewCinfoTrjBnc, String> {
	
	@Query(value = "SELECT TRUNC(FECHA_TRX),NUM_TIENDA,COUNT(1) "
			+ "     FROM TRX_HDR "
			+ "     WHERE TIPO_TRX IN (1,9,10) "
			+ "     AND FECHA_TRX BETWEEN TO_DATE(:pFecha,'YYYY-MM-DD') AND TO_DATE(:pFechaFin,'YYYY-MM-DD')  "
			//+ "     AND TRUNC(FECHA_TRX) = TO_DATE(:pFecha,'YYYY-MM-DD') "
			//+ "     and NUM_TIENDA in (1010,1080) "
			+ "     GROUP BY TRUNC(FECHA_TRX),NUM_TIENDA "
			+ "     ORDER BY TRUNC(FECHA_TRX), NUM_TIENDA ASC ", nativeQuery = true)
	public List<Object[]> findVentasPorAnio(@Param("pFecha") String pFecha, @Param("pFechaFin") String pFechaFin);
	
	@Query(value = "SELECT COUNT(1) "
			+ " FROM TRX_HDR "
			+ " WHERE TIPO_TRX IN (9,10,1) "
			+ " AND   NUM_TIENDA = :pTienda "
			+ " AND TRUNC(FECHA_TRX) = TO_DATE(:pFecha,'YYYY-MM-DD') ", nativeQuery = true)
	public Integer totalVentas(@Param("pFecha") String pFecha, @Param("pTienda") Integer pTienda);
	
	
	@Query(value = "SELECT COUNT(1) FROM (SELECT "
			+ "TRX_DET.NUM_TRX	TICKET, "
			+ "TRUNC(TRX_DET.FECHA_TRX)	FECHA_TICKET, "
			+ "TRX_DET.NUM_TIENDA	TIENDA, "
			+ "TRX_DET.NUM_CAJA	CAJA, "
			+ "TRX_DET.NUM_DOC_CANAL	NUM_DOC_CANAL, "
			+ "TRX_DET.NUM_lINEA_CANAL	CANAL_LINIO, "
			+ "COUNT(*)	TOTAL_ARTICULO, "
			+ "sysdate FechaCarga "
			+ "FROM TRX_HDR ,  "
			+ "            TRX_DET  "
			+ "WHERE TRX_HDR.NUM_TRX = TRX_DET.NUM_TRX "
			+ "AND TRX_HDR.TIPO_TRX IN (9,10,1) "
			+ "AND TRX_DET.NUM_DOC_CANAL IS NOT NULL "
			+ "AND TRUNC(TRX_HDR.FECHA_TRX) = TO_DATE(:pFecha,'YYYY-MM-DD') "
			+ "AND TRX_DET.NUM_TIENDA = :pTienda "
			+ "GROUP BY  "
			+ "TRX_DET.NUM_TRX	, "
			+ "TRX_DET.FECHA_TRX	, "
			+ "TRX_DET.NUM_TIENDA	, "
			+ "TRX_DET.NUM_CAJA, "
			+ "TRX_DET.NUM_DOC_CANAL, "
			+ "TRX_DET.NUM_lINEA_CANAL ) A", nativeQuery = true)
	public Integer totalVentasDet(@Param("pFecha") String pFecha
			                          , @Param("pTienda") Integer pTienda);

	@Query(value = "SELECT "
			+ " NUM_TRX	TICKET, "
			+ " TRUNC(FECHA_TRX)	FECHA_TICKET, "
			+ " NUM_TIENDA	TIENDA, "
			+ " NUM_CAJA	CAJA, "
			+ " NUM_TICKET	TRANSACCION, "
			+ " TIPO_TRX	TIPO, "
			+ " MNT_TOTAL_A_PAGAR	TOTAL, "
			+ " MNT_TOT_SN_IMPTOS	SUBTOTAL, "
			+ " MNT_REDONDEO	REDONDEO, "
			+ " TRX_ORIGINAL	TICKET_ORIGEN, "
			+ " TRUNC(FECHA_BCT)	FECHA_ENLACE, "
			+ " sysdate FECHA_CARGA "
			+ " FROM TRX_HDR "
			+ " WHERE TIPO_TRX IN (9,10,1) "
			+ " AND   NUM_TIENDA = :pTienda "
			+ " AND TRUNC(FECHA_TRX) = TO_DATE(:pFecha,'YYYY-MM-DD') "
			+ " ORDER BY TRUNC(FECHA_TICKET), TIENDA ", nativeQuery = true)
	public List<Object[]> findVentas(@Param("pFecha") String pFecha, @Param("pTienda") Integer pTienda);
	
	
	@Query(value = "SELECT "
			+ "TRX_DET.NUM_TRX	TICKET, "
			+ "TRUNC(TRX_DET.FECHA_TRX)	FECHA_TICKET, "
			+ "TRX_DET.NUM_TIENDA	TIENDA, "
			+ "TRX_DET.NUM_CAJA	CAJA, "
			+ "TRX_DET.NUM_DOC_CANAL	NUM_DOC_CANAL, "
			+ "TRX_DET.NUM_lINEA_CANAL	CANAL_LINIO, "
			+ "COUNT(*)	TOTAL_ARTICULO, "
			+ "sysdate FechaCarga "
			+ "FROM TRX_HDR ,  "
			+ "            TRX_DET  "
			+ "WHERE TRX_HDR.NUM_TRX = TRX_DET.NUM_TRX "
			+ "AND TRX_HDR.TIPO_TRX IN (9,10,1) "
			+ "AND TRX_DET.NUM_DOC_CANAL IS NOT NULL "
			+ "AND TRUNC(TRX_HDR.FECHA_TRX) = TO_DATE(:pFecha,'YYYY-MM-DD') "
			+ "AND TRX_DET.NUM_TIENDA = :pTienda "
			+ "GROUP BY  "
			+ "TRX_DET.NUM_TRX	, "
			+ "TRX_DET.FECHA_TRX	, "
			+ "TRX_DET.NUM_TIENDA	, "
			+ "TRX_DET.NUM_CAJA, "
			+ "TRX_DET.NUM_DOC_CANAL, "
			+ "TRX_DET.NUM_lINEA_CANAL", nativeQuery = true)
	public List<Object[]> findVentasDet(@Param("pFecha") String pFecha
			                          , @Param("pTienda") Integer pTienda);

}
