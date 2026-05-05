package com.sodimac.bctfacturacion.repository.bct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.bct.TrxPointsEntity;

@Repository
public interface TrxPointsRepository extends JpaRepository<TrxPointsEntity, Integer> {

	@Query(value = 
	""
	+ "WITH VW_MONTO_PUNTOS AS ( \r\n"
	+ "    SELECT A.NUM_TRX, A.TIPO_TRX, TO_NUMBER(c.f_conv) f_conv \r\n"
	+ "    FROM TRX_HDR A \r\n"
	+ "    INNER JOIN sw_cem.TRX_POINTS B \r\n"
	+ "    ON (CASE WHEN A.TIPO_TRX = 1 THEN A.NUM_TRX ELSE A.TRX_ORIGINAL END) = ( TO_CHAR( \"DATE\",'YYYYMMDD') || B.BRANCH || lpad(B.POS, 3, '0') || lpad(B.SEQUENCE, 4, '0')) \r\n"
	+ "    INNER JOIN sw_cem.TRX_POINTS_EXT C \r\n"
	+ "    ON B.ID = C.ID \r\n"
	+ "    WHERE A.TIPO_TRX IN (1, 9, 10) \r\n"
	+ "    AND B.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(B.IDENTIFICATIONNUMBER) = 10 \r\n"
	+ "    AND   NVL(B.POINTS,0) != 0 \r\n"

	+ ") \r\n"
	
    // Query de devolución de asignación de puntos registra la información en AdminPuntos	
	+ "select\r\n"
	+ "    hdr.NUM_TRX ticket \r\n"
	+ ",   TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta \r\n"
	+ ",   hdr.NUM_TIENDA tienda \r\n"
	+ ",   hdr.TIPO_TRX tipoTransaccion \r\n"
	+ ",   hdr.MNT_TOTAL_A_PAGAR montoVenta \r\n"
	+ ",   hdr.MNT_TOT_SN_IMPTOS montoSinImpuestos \r\n"
	+ ",   hdr.MNT_REDONDEO montoRedondeo \r\n"
	+ ",   SUM(( (SELECT distinct X.f_conv FROM VW_MONTO_PUNTOS X where X.NUM_TRX = hdr.NUM_TRX and X.TIPO_TRX IN (9, 10) ) * TO_NUMBER(p.POINTS) )) montoPuntos \r\n"
	+ ",   p.ID idPuntos \r\n"
	+ ",   p.TRANSACTIONTYPE tipoTransaccionCes \r\n"
	+ ",   p.POINTS puntos \r\n"
	+ "from  TRX_HDR hdr \r\n"
	+ "    , sw_cem.TRX_POINTS p \r\n"
	+ "where ( TO_CHAR( \"DATE\",'YYYYMMDD') || p.BRANCH || lpad(p.POS, 3, '0') || lpad(p.SEQUENCE, 4, '0')) = hdr.NUM_TRX \r\n"
	+ "AND   hdr.TIPO_TRX IN (9, 10) \r\n"
	+ "AND   hdr.NUM_TRX in (select fp.NUM_TRX from TRX_FRM_PAGO fp where fp.NUM_TRX = hdr.NUM_TRX and fp.TIPO_FORMA_PAGO != 2) \r\n"
	+ "AND   p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10 \r\n"
	+ "AND   NVL(p.POINTS,0) != 0 \r\n"
	+ "AND   trunc(\"DATE\") = TO_DATE(:pFecha,'YYYY-MM-DD') \r\n"
	+ "AND   trunc(hdr.FECHA_TRX) > TO_DATE('2025-01-01','YYYY-MM-DD') \r\n"
	+ "AND p.TRANSACTIONTYPE in ('cn') \r\n"
	+ "group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR, \r\n"
	+ "hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS \r\n"

	+ "union \r\n"
	
	// Query para asignación de puntos impacta a la tabla AdminPuntosCes
	+ "select \r\n"
	+ "    hdr.NUM_TRX ticket \r\n"
	+ ",   TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta \r\n"
	+ ",   hdr.NUM_TIENDA tienda \r\n"
	+ ",   hdr.TIPO_TRX tipoTransaccion \r\n"
	+ ",   hdr.MNT_TOTAL_A_PAGAR montoVenta \r\n"
	+ ",   hdr.MNT_TOT_SN_IMPTOS montoSinImpuestos \r\n"
	+ ",   hdr.MNT_REDONDEO montoRedondeo \r\n"
	+ ",   SUM(( (SELECT distinct X.f_conv FROM VW_MONTO_PUNTOS X where X.NUM_TRX = hdr.NUM_TRX) * TO_NUMBER(p.POINTS) )) montoPuntos \r\n"
	+ ",   p.ID idPuntos \r\n"
	+ ",   p.TRANSACTIONTYPE tipoTransaccionCes \r\n"
	+ ",   p.POINTS puntos \r\n"
	+ "from  TRX_HDR hdr \r\n"
	+ "    , sw_cem.TRX_POINTS p \r\n"
	+ "where ( TO_CHAR( \"DATE\",'YYYYMMDD') || p.BRANCH || lpad(p.POS, 3, '0') || lpad(p.SEQUENCE, 4, '0')) = hdr.NUM_TRX \r\n"
	+ "AND   hdr.TIPO_TRX IN (1, 9, 10) \r\n"
	+ "AND   hdr.NUM_TRX in (select fp.NUM_TRX from TRX_FRM_PAGO fp where fp.NUM_TRX = hdr.NUM_TRX and fp.TIPO_FORMA_PAGO != 2) \r\n"
	+ "AND   p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10 \r\n"
	+ "AND   NVL(p.POINTS, 0) != 0 \r\n"
	+ "AND   trunc(\"DATE\") = TO_DATE(:pFecha,'YYYY-MM-DD') \r\n"
	+ "AND   trunc(hdr.FECHA_TRX) > TO_DATE('2025-01-01','YYYY-MM-DD') \r\n"
	+ "group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR, \r\n"
	+ "hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS \r\n"

	+ "union \r\n"
	
	// Query de cancelación de provisiones de ticket
	+ "select \r\n"
	+ "    hdr.NUM_TRX ticket \r\n"
	+ ",   TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta \r\n"
	+ ",   hdr.NUM_TIENDA tienda \r\n"
	+ ",   hdr.TIPO_TRX tipoTransaccion \r\n"
	+ ",   hdr.MNT_TOTAL_A_PAGAR montoVenta \r\n"
	+ ",   hdr.MNT_TOT_SN_IMPTOS montoSinImpuestos \r\n"
	+ ",   hdr.MNT_REDONDEO montoRedondeo \r\n"
	+ ",   SUM(( (SELECT distinct X.f_conv FROM VW_MONTO_PUNTOS X where X.NUM_TRX = hdr.NUM_TRX ) * TO_NUMBER(p.POINTS) )) montoPuntos \r\n"
	+ ",   p.ID idPuntos \r\n"
	+ ",   p.TRANSACTIONTYPE tipoTransaccionCes \r\n"
	+ ",   p.POINTS puntos \r\n"
	+ "from  TRX_HDR hdr \r\n"
	+ "    , sw_cem.TRX_POINTS p \r\n"
	+ "where ( TO_CHAR( \"DATE\",'YYYYMMDD') || p.BRANCH || lpad(p.POS, 3, '0') || lpad(p.SEQUENCE, 4, '0')) = hdr.NUM_TRX \r\n"
	+ "AND   hdr.TIPO_TRX IN (1, 9, 10) \r\n"
	+ "AND   hdr.NUM_TRX in (select fp.NUM_TRX from TRX_FRM_PAGO fp where fp.NUM_TRX = hdr.NUM_TRX and fp.TIPO_FORMA_PAGO = 2) \r\n"
	+ "AND   p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10 \r\n"
	+ "AND   NVL(p.POINTS,0) != 0 \r\n"
	+ "AND   trunc(\"DATE\") = TO_DATE(:pFecha,'YYYY-MM-DD') \r\n"
	+ "AND   trunc(hdr.FECHA_TRX) > TO_DATE('2025-01-01','YYYY-MM-DD') \r\n"
	+ "group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR, \r\n"
	+ "hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS \r\n"

	+ "union \r\n"
	
	// Query puntos expirados
	+ "select\r\n"
	+ "    hdr.NUM_TRX ticket\r\n"
	+ ",   TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta\r\n"
	+ ",   hdr.NUM_TIENDA tienda\r\n"
	+ ",   hdr.TIPO_TRX tipoTransaccion\r\n"
	+ ",   hdr.MNT_TOTAL_A_PAGAR montoVenta\r\n"
	+ ",   hdr.MNT_TOT_SN_IMPTOS montoSinImpuestos\r\n"
	+ ",   hdr.MNT_REDONDEO montoRedondeo\r\n"
	+ ",   SUM(( TO_NUMBER(ext.f_conv) * TO_NUMBER(p.POINTS) )) montoPuntos\r\n"
	+ ",   p.ID idPuntos\r\n"
	+ ",   p.TRANSACTIONTYPE tipoTransaccionCes\r\n"
	+ ",   p.POINTS puntos\r\n"
	+ "from  TRX_HDR hdr\r\n"
	+ "    , sw_cem.TRX_POINTS p\r\n"
	+ "    , SW_CEM.trx_points_ext ext\r\n"
	+ "where ext.id = (select p2.id from sw_cem.TRX_POINTS p2 where ( TO_CHAR( \"DATE\",'YYYYMMDD') || p2.BRANCH || lpad(p2.POS, 3, '0') || lpad(p2.SEQUENCE, 4, '0')) = hdr.NUM_TRX and p2.TRANSACTIONTYPE='sale')\r\n"
	+ "AND   ( TO_CHAR( \"DATE\",'YYYYMMDD') || p.BRANCH || lpad(p.POS, 3, '0') || lpad(p.SEQUENCE, 4, '0')) = hdr.NUM_TRX\r\n"
	+ "      and   p.TRANSACTIONTYPE = 'exp_ces'\r\n"
	+ "AND   trunc(\"DATE\") between TO_DATE(:pFecha,'YYYY-MM-DD')-380 and TO_DATE(:pFecha,'YYYY-MM-DD')-365 \r\n"
	+ "AND   p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10 \r\n"
	+ "AND   NVL(p.POINTS,0) != 0 \r\n"
	+ "AND   trunc(hdr.FECHA_TRX) > TO_DATE('2025-01-01','YYYY-MM-DD') \r\n"
	+ "group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR,\r\n"
	+ "hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS\r\n"
		
	+ "ORDER BY 1"
	, nativeQuery = true)
	
	public List<Object[]> findPuntosCes(@Param("pFecha") String pFecha);


	@Query(value =
		"select \r\n"
		+ "    hdr.NUM_TRX ticket \r\n"
		+ ",   TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta \r\n"
		+ ",   hdr.NUM_TIENDA tienda \r\n"
		+ ",   hdr.TIPO_TRX tipoTransaccion \r\n"
		+ ",   hdr.MNT_TOTAL_A_PAGAR montoVenta \r\n"
		+ ",   hdr.MNT_TOT_SN_IMPTOS montoSinImpuestos \r\n"
		+ ",   hdr.MNT_REDONDEO montoRedondeo \r\n"
		+ ",   SUM(((SELECT distinct ext.f_conv FROM SW_CEM.trx_points_ext ext WHERE ext.id = p.ID) * TO_NUMBER(p.POINTS))) montoPuntos \r\n"
		+ ",   p.ID idPuntos \r\n"
		+ ",   p.TRANSACTIONTYPE tipoTransaccionCes \r\n"
		+ ",   p.POINTS puntos \r\n"
		+ "from  TRX_HDR hdr \r\n"
		+ "    , sw_cem.TRX_POINTS p \r\n"
		+ "where (TO_CHAR(\"DATE\",'YYYYMMDD') || p.BRANCH || lpad(p.POS, 3, '0') || lpad(p.SEQUENCE, 4, '0')) = hdr.NUM_TRX \r\n"
		+ "AND   hdr.NUM_TRX IN (:pTickets) \r\n"
		+ "AND   p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10 \r\n"
		+ "AND   NVL(p.POINTS,0) != 0 \r\n"
		+ "AND   p.TRANSACTIONTYPE = 'sale' \r\n"
		+ "group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR, \r\n"
		+ "hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS \r\n"
		+ "ORDER BY 1"
	, nativeQuery = true)
	public List<Object[]> findPuntosCesByTickets(@Param("pTickets") List<String> pTickets);


	@Query(value = "select ID "
			+ "     , SKU "
			+ "     , CASE WHEN returned > 0 THEN returned ELSE POINTS END POINTS "
			+ "from sw_cem.TRX_POINTS_SKU "
			+ "where id = :pIdPuntos", nativeQuery = true)
	public List<Object[]> findSkuPuntosById(@Param("pIdPuntos") Long pIdPuntos);
	
	
	@Query(value = "SELECT A.NUM_TRX "
			+ "      , TO_CHAR(A.FECHA_TRX,'YYYY-MM-DD') FECHA_TRX "
			+ "      , A.NUM_TIENDA "
			+ "      , A.TIPO_TRX "
			+ "      , A.MNT_TOTAL_A_PAGAR "
			+ "      , A.MNT_TOT_SN_IMPTOS "
			+ "      , A.MNT_REDONDEO "
			+ "  FROM TRX_HDR A "
			+ "  WHERE A.NUM_TRX = :pTicket", nativeQuery = true)
	public List<Object[]> findByTicket(@Param("pTicket") String pTicket);
	
	
	@Query(value = "SELECT B.NUM_TRX "
			+ "       , B.NUM_LINEA "
			+ "       , B.ORDEN_IMPTO "
			+ "       , A.SKU "
			+ "       , A.DV_SKU "
			+ "       , A.DESCRIPCION "
			+ "       , B.TOTAL_LINEA "
			+ "       , B.TIPO_IMPTO "
			+ "       , B.ID_TASA_IMPTO "
			+ "       , B.MNT_IMPTO "
			+ "       , B.PCTJE_IMPTO "
			+ "  FROM TRX_DET A "
			+ "     , TRX_DET_IMPUESTO B "
			+ "  WHERE A.NUM_TRX = B.NUM_TRX "
			+ "  AND A.UPC = B.UPC_CODE "
			+ "  AND A.NUM_LINEA = B.NUM_LINEA "
			+ "  AND A.NUM_TRX = :pTicket "
			+ "  ORDER BY A.NUM_LINEA", nativeQuery = true)
	public List<Object[]> findImpuestosByTicket(@Param("pTicket") String pTicket);
	
}
