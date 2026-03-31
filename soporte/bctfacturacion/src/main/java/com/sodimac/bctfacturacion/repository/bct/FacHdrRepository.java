package com.sodimac.bctfacturacion.repository.bct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.bct.FacHdrEntity;

@Repository
public interface FacHdrRepository extends JpaRepository<FacHdrEntity, Long> {

	@Query(value = "WITH VW_FACTURAS AS(\r\n"
			+ "	SELECT A.UUID\r\n"
			+ "		 , B.NUM_TRX\r\n"
			+ "	FROM FAC_HDR A\r\n"
			+ "	   , FAC_DET_TICKETS B\r\n"
			+ "	WHERE A.ID_PROCESO = B.ID_PROCESO\r\n"
			+ "	AND A.TIPO_OPERACION = 'V' \r\n"
			+ "    AND A.F_VENTA >= TO_DATE('01/01/' || TO_CHAR(SYSDATE, 'YYYY'), 'DD/MM/YYYY') \r\n"
			+ "    AND A.UUID IS NOT NULL\r\n"
			+ ") \r\n"
			+ "	SELECT A.NUM_TIENDA\r\n"
			+ "	     , TO_CHAR(A.FECHA_TRX, 'DD/MM/YYYY') FECHA_TRX\r\n"
			+ "		 , 'V', COUNT(1) TOTAL \r\n"
			+ "	FROM TRX_HDR A\r\n"
			+ "	WHERE A.TIPO_TRX = 1 \r\n"
			+ "	AND A.FECHA_TRX BETWEEN TO_DATE('01/01/' || TO_CHAR(SYSDATE, 'YYYY')) \r\n"
			+ "	AND TRUNC(SYSDATE - 1) \r\n"
			+ "	AND A.FECHA_TRX >= TO_DATE(:pFecha, 'DD/MM/YYYY') \r\n"
			+ "	AND NOT EXISTS (\r\n"
			+ "    SELECT 1\r\n"
			+ "	FROM VW_FACTURAS D\r\n"
			+ "	WHERE D.NUM_TRX = A.NUM_TRX\r\n"
			+ "	AND D.UUID IS NOT NULL\r\n"
			+ "  ) \r\n"
			+ "GROUP BY A.NUM_TIENDA,TO_CHAR(A.FECHA_TRX, 'DD/MM/YYYY') \r\n"
			+ "ORDER BY COUNT(1) DESC\r\n"
			+ "", nativeQuery = true)
	public List<Object[]> findTimbradoGlobalVenta(@Param("pFecha") String pFecha);
}
