package com.sodimac.facturacion.repository.bct;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.facturacion.exception.DataBaseException;
import com.sodimac.facturacion.models.Cabecera;
import com.sodimac.facturacion.models.ConceptoTicket;
import com.sodimac.facturacion.models.DatosControl;
import com.sodimac.facturacion.models.DatosExtra;
import com.sodimac.facturacion.models.Emisor;
import com.sodimac.facturacion.models.ErrorBase;
import com.sodimac.facturacion.models.Ticket;
import com.sodimac.facturacion.models.TotalesTicket;
import com.sodimac.facturacion.models.TrasladoConcepto;
import com.sodimac.facturacion.repository.DataBaseFactory;

import oracle.jdbc.OracleTypes;

public class QueryBctRepository {

	private static final Logger logger = LoggerFactory.getLogger(QueryBctRepository.class);
	private Connection conn;
	
	static final Integer I_OC = 1;
	static final Integer I_NO_TICKET = 2;
	static final Integer I_TIENDA = 3;
	static final Integer I_CAJA = 4;
	static final Integer I_FECHA_TICKET = 5;
	
	static final Integer I_DETALLE_TICKET = 6;
	static final Integer I_IMPUESTOS = 7;
	static final Integer I_VERSION = 8;
	static final Integer I_SERIE = 9;
	static final Integer I_FOLIO = 10;
	static final Integer I_FECHA = 11;
	static final Integer I_FORMA_PAGO = 12;
	static final Integer I_CONDICIONES_PAGO = 13;
	static final Integer I_METODO_PAGO = 14;
	static final Integer I_TIPO_COMPROBANTE = 15;
	static final Integer I_LUGAR_EXP = 16;
	static final Integer I_RFC_EMISOR = 17;
	static final Integer I_NOMBRE_EMISOR = 18;
	static final Integer I_RFISCAL_EMISOR = 19;	
	static final Integer I_TOTAL = 20;
	static final Integer I_SUBTOTAL = 21;
	static final Integer I_DESCUENTO = 22;
	static final Integer I_TIPO_CAMBIO = 23;
	static final Integer I_IMPORTE_LETRA = 24;
	static final Integer I_IMPUESTOS_TRASLADOS = 25;
	static final Integer I_CTL_ID_CFDI = 26;
	static final Integer I_CTL_ID_STATUS = 27;
	static final Integer I_CTL_STATUS_IMPRESION = 28;
	static final Integer I_CTL_STATUS_CORREO = 29;
	static final Integer I_CTL_STATUS_ARCHIVO = 30;
	static final Integer I_CTL_ID_RECHAZO = 31;
	static final Integer I_CTL_ID_COMPLEMENTO = 32;
	static final Integer I_EXTRA_1 = 33;
	static final Integer I_EXTRA_2 = 34;
	static final Integer I_NUM_FACTURA = 35;
	static final Integer I_CTL_COD_ERROR = 36;
	static final Integer I_CTL_DESC_ERROR = 37;
	
	private static final String ERR_NO_DATA = "77";


	public Ticket getDatosTicket(String oc, int noTicket, String tienda, String caja, Date fecha)
			throws DataBaseException {

		String sql = "{ call SW_FAC.PKG_CFDI_CLI.GET_DATOS_TICKET (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
		Ticket result = null;
		CallableStatement callableStatement = null;
		long startTime = 0;

		try {
			conn = DataBaseFactory.getConn();
			callableStatement = conn.prepareCall(sql);

			callableStatement.setString(I_OC, oc);
			callableStatement.setInt(I_NO_TICKET, noTicket);
			callableStatement.setString(I_TIENDA, tienda);
			callableStatement.setString(I_CAJA, caja);
			callableStatement.setDate(I_FECHA_TICKET, fecha);
//33
			callableStatement.registerOutParameter(I_DETALLE_TICKET, OracleTypes.CURSOR); //o_detalle_ticket | CURSOR
			callableStatement.registerOutParameter(I_IMPUESTOS, OracleTypes.CURSOR); //o_impuestos | CURSOR
			callableStatement.registerOutParameter(I_VERSION, java.sql.Types.VARCHAR); //o_f_version | VARCHAR2
			callableStatement.registerOutParameter(I_SERIE, java.sql.Types.VARCHAR); //o_f_serie | VARCHAR2
			callableStatement.registerOutParameter(I_FOLIO, java.sql.Types.NUMERIC); //o_folio | NUMBER
			callableStatement.registerOutParameter(I_FECHA, java.sql.Types.VARCHAR); //o_fecha | VARCHAR2
			callableStatement.registerOutParameter(I_FORMA_PAGO, java.sql.Types.VARCHAR); //o_forma_pago | VARCHAR2
			callableStatement.registerOutParameter(I_CONDICIONES_PAGO, java.sql.Types.VARCHAR); //o_condiciones_pago | VARCHAR2
			callableStatement.registerOutParameter(I_METODO_PAGO, java.sql.Types.VARCHAR); //o_metodo_pago | VARCHAR2
			callableStatement.registerOutParameter(I_TIPO_COMPROBANTE, java.sql.Types.VARCHAR); //o_tipo_comprobante | VARCHAR2
			callableStatement.registerOutParameter(I_LUGAR_EXP, java.sql.Types.CHAR); //o_lugar_exp | CHAR
			callableStatement.registerOutParameter(I_RFC_EMISOR, java.sql.Types.VARCHAR); //o_rfc_emisor V| ARCHAR2
			callableStatement.registerOutParameter(I_NOMBRE_EMISOR, java.sql.Types.VARCHAR); //o_nombre_emisor | VARCHAR2
			callableStatement.registerOutParameter(I_RFISCAL_EMISOR, java.sql.Types.VARCHAR); //o_rfiscal_emisor | VARCHAR2		
			callableStatement.registerOutParameter(I_TOTAL, java.sql.Types.VARCHAR); //o_total | VARCHAR2
			callableStatement.registerOutParameter(I_SUBTOTAL, java.sql.Types.VARCHAR); //o_sub_total | VARCHAR2
			callableStatement.registerOutParameter(I_DESCUENTO, java.sql.Types.VARCHAR); //o_descuento | VARCHAR2
			callableStatement.registerOutParameter(I_TIPO_CAMBIO, java.sql.Types.VARCHAR); //o_tipo_cambio | VARCHAR2
			callableStatement.registerOutParameter(I_IMPORTE_LETRA, java.sql.Types.VARCHAR); //o_importe_letra | VARCHAR2
			callableStatement.registerOutParameter(I_IMPUESTOS_TRASLADOS, java.sql.Types.VARCHAR); //o_impuestos_traslados | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_ID_CFDI, java.sql.Types.VARCHAR); //o_ctl_id_cfdi | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_ID_STATUS, java.sql.Types.VARCHAR); //o_ctl_id_status | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_STATUS_IMPRESION, java.sql.Types.VARCHAR); //o_ctl_estatus_impresion | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_STATUS_CORREO, java.sql.Types.VARCHAR); //o_ctl_estatus_correo | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_STATUS_ARCHIVO, java.sql.Types.VARCHAR); //o_ctl_estatus_archivo | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_ID_RECHAZO, java.sql.Types.VARCHAR); //o_ctl_id_rechazo | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_ID_COMPLEMENTO, java.sql.Types.VARCHAR); //o_ctl_id_complemento | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_COD_ERROR, java.sql.Types.VARCHAR); //o_cod_error | VARCHAR2
			callableStatement.registerOutParameter(I_CTL_DESC_ERROR, java.sql.Types.VARCHAR); //o_desc_error | VARCHAR2
			callableStatement.registerOutParameter(I_EXTRA_1, java.sql.Types.VARCHAR); //o_extra_1 | VARCHAR2
			callableStatement.registerOutParameter(I_EXTRA_2, java.sql.Types.VARCHAR); //o_extra_2 | VARCHAR2
			callableStatement.registerOutParameter(I_NUM_FACTURA, java.sql.Types.VARCHAR); //o_num_factura | VARCHAR2

			// Log antes de ejecutar el SP
			startTime = System.currentTimeMillis();
			logger.info("========== INICIO EJECUCION SP ORACLE ==========");
			logger.info("Stored Procedure: PKG_FACTURA_UNITARIA.GET_DATOS_TICKET");
			logger.info("Parametros - OC: {}, Ticket: {}, Tienda: {}, Caja: {}, Fecha: {}",	oc, noTicket, tienda, caja, fecha);

			callableStatement.execute();
			
			result = new Ticket();

			// Log después de ejecutar el SP exitosamente
			long endTime = System.currentTimeMillis();
			long durationMillis = endTime - startTime;
			long minutes = (durationMillis / 1000) / 60;
			long seconds = (durationMillis / 1000) % 60;
			long millis = durationMillis % 1000;

			logger.info("========== FIN EJECUCION SP ORACLE (EXITOSA) ==========");
			logger.info("Tiempo de ejecucion: {} minutos, {} segundos, {} milisegundos", minutes, seconds, millis);
			logger.info("Tiempo total en milisegundos: {}ms", durationMillis);
			
			String codigoMensaje = callableStatement.getString(I_CTL_COD_ERROR);
			String mensaje = callableStatement.getString(I_CTL_DESC_ERROR);			
			result.setError(new ErrorBase(codigoMensaje, mensaje));
			
			if(codigoMensaje.equals(ERR_NO_DATA)) {
				return result;
			}
			
			Cabecera cabecera = new Cabecera();
			cabecera.setVersion(callableStatement.getString(I_VERSION));
			cabecera.setFecha(callableStatement.getString(I_FECHA));
			cabecera.setFolio(callableStatement.getString(I_FOLIO));
			cabecera.setFormaPago(callableStatement.getString(I_FORMA_PAGO));
			cabecera.setLugarExpedicion(callableStatement.getString(I_LUGAR_EXP));
			cabecera.setMetodoPago(callableStatement.getString(I_METODO_PAGO));
			cabecera.setSerie(callableStatement.getString(I_SERIE));
			cabecera.setTipoComprobante(callableStatement.getString(I_TIPO_COMPROBANTE));
			cabecera.setVersion(callableStatement.getString(I_VERSION));
			
			List<ConceptoTicket> conceptos = ConceptosMapper.readConceptosFromCursor(callableStatement);
			List<TrasladoConcepto> impuestos = new ArrayList<TrasladoConcepto>();
			
			try (ResultSet rs = (ResultSet) callableStatement.getObject(I_IMPUESTOS)) {
				while (rs.next()) {
					TrasladoConcepto tc = new TrasladoConcepto();
					tc.setImpuesto(rs.getString(1));
					tc.setTasaOCuota(new BigDecimal(rs.getString(2)));
					tc.setImporte(new BigDecimal(rs.getString(3)));
					tc.setOrdenador(rs.getLong(4));
					impuestos.add(tc);					
				}
			}
			
			Emisor emisor = new Emisor();
			emisor.setNombre(callableStatement.getString(I_NOMBRE_EMISOR));
			emisor.setRegimenFiscal(callableStatement.getString(I_RFISCAL_EMISOR));
			emisor.setRfc(callableStatement.getString(I_RFC_EMISOR));
			
			TotalesTicket totales = new TotalesTicket();
			totales.setDescuento(callableStatement.getString(I_DESCUENTO));
			totales.setImporteLetra(callableStatement.getString(I_IMPORTE_LETRA));
			totales.setSubtotal(callableStatement.getString(I_SUBTOTAL));
			totales.setTipoCambio(callableStatement.getString(I_TIPO_CAMBIO));
			totales.setTotal(callableStatement.getString(I_TOTAL));
			totales.setTotalImpuestosTrasladados(callableStatement.getString(I_IMPUESTOS_TRASLADOS));
			
			DatosControl control = new DatosControl();
			control.setEstatusArchivo(callableStatement.getString(I_CTL_STATUS_ARCHIVO));
			control.setEstatusCorreo(callableStatement.getString(I_CTL_STATUS_CORREO));
			control.setEstatusImpresion(callableStatement.getString(I_CTL_STATUS_IMPRESION));
			control.setIdCfdi(callableStatement.getString(I_CTL_ID_CFDI));
			control.setIdComplemento(callableStatement.getString(I_CTL_ID_COMPLEMENTO));
			control.setIdRechazo(callableStatement.getString(I_CTL_ID_RECHAZO));
			control.setIdStatus(callableStatement.getString(I_CTL_ID_STATUS));
			
			DatosExtra datosExtra = new DatosExtra();
			datosExtra.setExtra1(callableStatement.getString(I_EXTRA_1));
			datosExtra.setExtra2(callableStatement.getString(I_EXTRA_2));			
			
			result.setCabecera(cabecera);
			result.setEmisor(emisor);
			result.setImpuestos(impuestos);
			result.setConceptos(conceptos);
			result.setTotales(totales);
			result.setControl(control);
			result.setDatosExtra(datosExtra);			

		} catch (Exception e) {
			// Log después de error
			long endTime = System.currentTimeMillis();
			long durationMillis = endTime - startTime;
			long minutes = (durationMillis / 1000) / 60;
			long seconds = (durationMillis / 1000) % 60;
			long millis = durationMillis % 1000;

			logger.error("========== FIN EJECUCION SP ORACLE (ERROR) ==========");
			logger.error("Tiempo antes de fallar: {} minutos, {} segundos, {} milisegundos", minutes, seconds, millis);
			logger.error("Error al ejecutar PKG_FACTURA_UNITARIA.GENERAR_COMPROBANTE: {}", e.getMessage(), e);

			throw new DataBaseException(e);
		} finally {
			// Cerrar statement y conexión de forma síncrona
			try {
				if (callableStatement != null && !callableStatement.isClosed()) {
					callableStatement.close();
				}
			} catch (Exception e) {
				// Log pero no lanzar excepción
			}
			DataBaseFactory.close(conn);
		}

		return result;
	}

	public ErrorBase confirmaFactura(String ticket, String codigo, String uuid) throws DataBaseException {

		// Usar CallableStatement correctamente con parámetros bind en lugar de concatenación SQL
		String sql = "{ call PKG_CONFIRMAR_FACTURA.CONFIRMAR_FACTURA(?, ?, ?, ?, ?, ?) }";
		ErrorBase result = null;
		CallableStatement callableStatement = null;
		long startTime = 0;

		try {
			conn = DataBaseFactory.getConn();
			callableStatement = conn.prepareCall(sql);

			// Parámetros IN
			callableStatement.setString(1, uuid);  // I_CFDI
			callableStatement.setString(2, ticket);  // I_TICKETS
			callableStatement.setString(3, codigo);  // I_COD_ERROR
			callableStatement.setString(4, "");  // I_DET_ERROR

			// Parámetros OUT
			callableStatement.registerOutParameter(5, java.sql.Types.VARCHAR);  // O_COD_ERROR
			callableStatement.registerOutParameter(6, java.sql.Types.VARCHAR);  // O_DESC_ERROR

			// Log antes de ejecutar el SP
			startTime = System.currentTimeMillis();
			logger.info("========== INICIO EJECUCION SP ORACLE ==========");
			logger.info("Stored Procedure: PKG_CONFIRMAR_FACTURA.CONFIRMAR_FACTURA");
			logger.info("Parametros - UUID: {}, Ticket: {}, Codigo: {}", uuid, ticket, codigo);

			callableStatement.execute();

			// Log después de ejecutar el SP exitosamente
			long endTime = System.currentTimeMillis();
			long durationMillis = endTime - startTime;
			long minutes = (durationMillis / 1000) / 60;
			long seconds = (durationMillis / 1000) % 60;
			long millis = durationMillis % 1000;

			logger.info("========== FIN EJECUCION SP ORACLE (EXITOSA) ==========");
			logger.info("Tiempo de ejecucion: {} minutos, {} segundos, {} milisegundos", minutes, seconds, millis);
			logger.info("Tiempo total en milisegundos: {}ms", durationMillis);

			String codigoMensaje = callableStatement.getString(5);
			String mensaje = callableStatement.getString(6);

			result = new ErrorBase(codigoMensaje, mensaje);

		} catch (Exception e) {
			// Log después de error
			long endTime = System.currentTimeMillis();
			long durationMillis = endTime - startTime;
			long minutes = (durationMillis / 1000) / 60;
			long seconds = (durationMillis / 1000) % 60;
			long millis = durationMillis % 1000;

			logger.error("========== FIN EJECUCION SP ORACLE (ERROR) ==========");
			logger.error("Tiempo antes de fallar: {} minutos, {} segundos, {} milisegundos", minutes, seconds, millis);
			logger.error("Error al ejecutar PKG_CONFIRMAR_FACTURA.CONFIRMAR_FACTURA: {}", e.getMessage(), e);

			throw new DataBaseException(e);
		} finally {
			// Cerrar statement y conexión de forma síncrona
			try {
				if (callableStatement != null && !callableStatement.isClosed()) {
					callableStatement.close();
				}
			} catch (Exception e) {
				// Log pero no lanzar excepción
			}
			DataBaseFactory.close(conn);
		}

		return result;
	}

}