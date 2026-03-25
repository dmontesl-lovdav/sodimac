package com.sodimac.facturacion.repository.bct;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sodimac.facturacion.models.ConceptoTicket;
import com.sodimac.facturacion.models.TrasladoConcepto;

public class ConceptosMapper {
	
	private static final int CON_CLAVEPRODSERV = 1;
	private static final int CON_NOIDENTIFICACION = 2;
	private static final int CON_CANTIDAD = 3;
	private static final int CON_CLAVEUNIDAD = 4;
	private static final int CON_UNIDAD = 5;
	private static final int CON_DESCRIPCION = 6;
	private static final int CON_VALORUNITARIO = 7;
	private static final int CON_IMPORTE = 8;
	private static final int CON_DESCUENTO = 9;
	private static final int CON_ORDENADOR = 10;
	private static final int CON_PADRE = 11;
	private static final int CON_NIVEL = 12;
	private static final int TRA_BASE = 13;
	private static final int TRA_IMPUESTO = 14;
	private static final int TRA_TIPOFACTOR = 15;
	private static final int TRA_TASACUOTA = 16;
	private static final int TRA_IMPORTE = 17;
	private static final int TRA_ORDENADOR = 18;

	public static List<ConceptoTicket> readConceptosFromCursor(CallableStatement callableStatement) throws SQLException {

		try (ResultSet rs = (ResultSet) callableStatement.getObject(QueryBctRepository.I_DETALLE_TICKET)) {

			Map<String, ConceptoTicket> conceptosByKey = new LinkedHashMap<>();

			while (rs.next()) {
				
				String key = rs.getString(CON_NOIDENTIFICACION) + "|" + rs.getLong(CON_ORDENADOR) + "|" + rs.getLong(CON_PADRE) + "|" + rs.getInt(CON_NIVEL);

				ConceptoTicket concepto = conceptosByKey.computeIfAbsent(key, k -> {
					try {
						ConceptoTicket c = new ConceptoTicket();
						// columns 1..12 (parent)
						c.claveProdServ = rs.getString(CON_CLAVEPRODSERV);
						c.noIdentificacion = rs.getString(CON_NOIDENTIFICACION);
						c.cantidad = bd(rs, CON_CANTIDAD);
						c.claveUnidad = rs.getString(CON_CLAVEUNIDAD);
						c.unidad = rs.getString(CON_UNIDAD);
						c.descripcion = rs.getString(CON_DESCRIPCION);
						c.valorUnitario = bd(rs, CON_VALORUNITARIO);
						c.importe = bd(rs, CON_IMPORTE);
						c.descuento = bd(rs, CON_DESCUENTO);
						c.ordenador = lng(rs, CON_ORDENADOR);
						c.padre = lng(rs, CON_PADRE);
						c.nivel = integer(rs, CON_NIVEL);
						return c;
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});

				TrasladoConcepto t = new TrasladoConcepto();
				t.base = bd(rs, TRA_BASE);
				t.impuesto = rs.getString(TRA_IMPUESTO);
				t.tipoFactor = rs.getString(TRA_TIPOFACTOR);
				t.tasaOCuota = bd(rs, TRA_TASACUOTA);
				t.importe = bd(rs, TRA_IMPORTE);
				t.ordenador = lng(rs, TRA_ORDENADOR);

				String trasladoKey = String.valueOf(t.impuesto) + "|" + String.valueOf(t.tipoFactor) + "|" + String.valueOf(t.tasaOCuota) + "|" + String.valueOf(t.importe) + "|"
						+ String.valueOf(t.base);

				if (!containsTraslado(concepto.traslados, trasladoKey)) {
					concepto.traslados.add(t);
				}
			}

			return new ArrayList<>(conceptosByKey.values());
		}
	}

	private static boolean containsTraslado(List<TrasladoConcepto> list, String key) {
		for (TrasladoConcepto t : list) {
			String k = String.valueOf(t.impuesto) + "|" + String.valueOf(t.tipoFactor) + "|" + String.valueOf(t.tasaOCuota) + "|" + String.valueOf(t.importe) + "|"
					+ String.valueOf(t.base);
			if (k.equals(key))
				return true;
		}
		return false;
	}

	private static BigDecimal bd(ResultSet rs, int idx) throws SQLException {
		String s = rs.getString(idx);
		if (s == null)
			return null;
		s = s.trim();
		if (s.isEmpty())
			return null;
		return new BigDecimal(s);
	}

	private static Long lng(ResultSet rs, int idx) throws SQLException {
		long v = rs.getLong(idx);
		return rs.wasNull() ? null : v;
	}

	private static Integer integer(ResultSet rs, int idx) throws SQLException {
		int v = rs.getInt(idx);
		return rs.wasNull() ? null : v;
	}

}
